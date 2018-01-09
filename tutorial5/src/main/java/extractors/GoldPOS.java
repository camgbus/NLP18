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

import de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.*;

/**
 * Extracts the pos tag taken from the gold standard pos tag from the CoNLL file.
 */
public class GoldPOS extends FeatureExtractorResource_ImplBase
implements FeatureExtractor{
    public static final String FEATURE_NAME = "GoldPOS";

	public Set<Feature> extract(JCas view, TextClassificationTarget target) throws TextClassificationException {
		List<POS> posTags = JCasUtil.selectCovered(view, POS.class, target);
		return new Feature(FEATURE_NAME, posTags.get(0).toString().split("\\n|\\r")[0]).asSet();
	}

}
