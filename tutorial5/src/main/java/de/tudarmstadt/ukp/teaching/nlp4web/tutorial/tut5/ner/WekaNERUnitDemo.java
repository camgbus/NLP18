/**
 * Copyright 2016
 * Ubiquitous Knowledge Processing (UKP) Lab
 * Technische Universität Darmstadt
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see http://www.gnu.org/licenses/.
 */
package de.tudarmstadt.ukp.teaching.nlp4web.tutorial.tut5.ner;

import static de.tudarmstadt.ukp.dkpro.core.api.io.ResourceCollectionReaderBase.INCLUDE_PREFIX;
import static org.apache.uima.fit.factory.AnalysisEngineFactory.createEngineDescription;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.collection.CollectionReaderDescription;
import org.apache.uima.fit.component.NoOpAnnotator;
import org.apache.uima.fit.factory.CollectionReaderFactory;
import org.apache.uima.resource.ResourceInitializationException;
import org.dkpro.lab.Lab;
import org.dkpro.lab.task.BatchTask.ExecutionPolicy;
import org.dkpro.lab.task.Dimension;
import org.dkpro.lab.task.ParameterSpace;
import org.dkpro.tc.api.features.TcFeatureFactory;
import org.dkpro.tc.api.features.TcFeatureSet;
import org.dkpro.tc.core.Constants;
//Class was copied into project, though its not imported because it does not seem to be needed
//import org.dkpro.tc.examples.single.sequence.ContextMemoryReport;
import org.dkpro.tc.features.style.InitialCharacterUpperCase;
import org.dkpro.tc.features.style.IsSurroundedByChars;
import org.dkpro.tc.ml.ExperimentCrossValidation;
import org.dkpro.tc.ml.ExperimentTrainTest;
import org.dkpro.tc.ml.weka.WekaClassificationAdapter;

import weka.classifiers.bayes.NaiveBayes;

/**
 * This is an example for NER as unit classification. Each Entity is treated as a classification
 * unit. This is only a showcase of the concept.
 */
public class WekaNERUnitDemo
    implements Constants
{

    public static final String LANGUAGE_CODE = "de";
    public static final int NUM_FOLDS = 2;
    public static final String corpusFilePathTrain = "src/main/resources/data/germ_eval2014_ner/train";
    public static final String corpusFilePathTest = "src/main/resources/data/germ_eval2014_ner/test";

    public static void main(String[] args)
        throws Exception
    {
        // This is used to ensure that the required DKPRO_HOME environment variable is set.
        // Ensures that people can run the experiments even if they haven't read the setup
        // instructions first :)
        // Don't use this in real experiments! Read the documentation and set DKPRO_HOME as
        // explained there.
        DemoUtils.setDkproHome(WekaNERUnitDemo.class.getSimpleName());

        WekaNERUnitDemo demo = new WekaNERUnitDemo();
        demo.runTrainTest(getParameterSpace());
    }

    // ##### CV #####
    protected void runCrossValidation(ParameterSpace pSpace)
        throws Exception
    {
        ExperimentCrossValidation batch = new ExperimentCrossValidation("NERDemoCV",
                WekaClassificationAdapter.class, NUM_FOLDS);
        batch.setPreprocessing(getPreprocessing());
        batch.setParameterSpace(pSpace);
        batch.setExecutionPolicy(ExecutionPolicy.RUN_AGAIN);

        // Run
        Lab.getInstance().run(batch);
    }

    // ##### TrainTest #####
    public void runTrainTest(ParameterSpace pSpace)
        throws Exception
    {
        ExperimentTrainTest batch = new ExperimentTrainTest("NERDemoTrainTest",
                WekaClassificationAdapter.class);
        batch.setPreprocessing(getPreprocessing());
        batch.addReport(ContextMemoryReport.class);
        batch.setParameterSpace(pSpace);
        batch.setExecutionPolicy(ExecutionPolicy.RUN_AGAIN);
        // Run
        Lab.getInstance().run(batch);
    }

    public static ParameterSpace getParameterSpace()
        throws ResourceInitializationException
    {
        Map<String, Object> dimReaders = new HashMap<String, Object>();

        CollectionReaderDescription readerTrain = CollectionReaderFactory.createReaderDescription(
        		/* NERDemoReader.class, NERDemoReader.PARAM_LANGUAGE, "de",
        		 * NERDemoReader.PARAM_SOURCE_LOCATION, corpusFilePathTrain,
        		 * NERDemoReader.PARAM_PATTERNS, INCLUDE_PREFIX + "*.txt");
        		 */
        		NERCoNLLReader.class, NERCoNLLReader.PARAM_LANGUAGE, "en",
        		NERCoNLLReader.PARAM_SOURCE_LOCATION, corpusFilePathTrain,
        		NERCoNLLReader.PARAM_PATTERNS, INCLUDE_PREFIX + "*.train");
        dimReaders.put(DIM_READER_TRAIN, readerTrain);

        CollectionReaderDescription readerTest = CollectionReaderFactory.createReaderDescription(
        		/* NERDemoReader.class, NERDemoReader.PARAM_LANGUAGE, "de",
        		 * NERDemoReader.PARAM_SOURCE_LOCATION, corpusFilePathTrain,
        		 * NERDemoReader.PARAM_PATTERNS, INCLUDE_PREFIX + "*.txt");
        		 */
        		NERCoNLLReader.class, NERCoNLLReader.PARAM_LANGUAGE, "en",
        		NERCoNLLReader.PARAM_SOURCE_LOCATION, corpusFilePathTrain,
        		NERCoNLLReader.PARAM_PATTERNS, INCLUDE_PREFIX + "*.train");
        dimReaders.put(DIM_READER_TEST, readerTest);

        @SuppressWarnings("unchecked")
        Dimension<List<String>> dimClassificationArgs = Dimension.create(DIM_CLASSIFICATION_ARGS,
                Arrays.asList(new String[] { NaiveBayes.class.getName() }));

        Dimension<TcFeatureSet> dimFeatureSets = Dimension.create(
                Constants.DIM_FEATURE_SET,
                new TcFeatureSet(TcFeatureFactory.create(InitialCharacterUpperCase.class),
                        TcFeatureFactory.create(IsSurroundedByChars.class,
                                IsSurroundedByChars.PARAM_SURROUNDING_CHARS, "\"\"")));

        ParameterSpace pSpace = new ParameterSpace(Dimension.createBundle("readers", dimReaders),
                Dimension.create(DIM_LEARNING_MODE, LM_SINGLE_LABEL),
                Dimension.create(DIM_FEATURE_MODE, FM_UNIT), dimFeatureSets, dimClassificationArgs);

        return pSpace;
    }

    protected AnalysisEngineDescription getPreprocessing()
        throws ResourceInitializationException
    {
        return createEngineDescription(NoOpAnnotator.class);
    }
}
