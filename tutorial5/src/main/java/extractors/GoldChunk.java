package extractors;

import java.util.List;
import java.util.Set;

import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.dkpro.tc.api.exception.TextClassificationException;
import org.dkpro.tc.api.features.Feature;
import org.dkpro.tc.api.features.FeatureExtractor;
import org.dkpro.tc.api.features.FeatureExtractorResource_ImplBase;
import org.dkpro.tc.api.type.TextClassificationTarget;

import type.Chunk;

/**
 * Extracts the chunk tag taken from the gold standard chunk tag from the CoNLL file.
 */
public class GoldChunk extends FeatureExtractorResource_ImplBase
implements FeatureExtractor{
    public static final String FEATURE_NAME = "GoldChunk";

	public Set<Feature> extract(JCas view, TextClassificationTarget target) throws TextClassificationException {
		List<Chunk> chunkTags = JCasUtil.selectCovered(view, Chunk.class, target);
		return new Feature(FEATURE_NAME, chunkTags.get(0).toString().split("\\n|\\r")[0]).asSet();
	}

}
