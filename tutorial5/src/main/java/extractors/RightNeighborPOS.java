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

import de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.POS;

/**
 * Extracts the POS tag from the following token.
 */
public class RightNeighborPOS extends FeatureExtractorResource_ImplBase implements FeatureExtractor{

    public static final String FEATURE_NAME = "RightPOS";

	public Set<Feature> extract(JCas view, TextClassificationTarget target) throws TextClassificationException {
		List<POS> posTags = JCasUtil.selectCovered(view, POS.class, target);
		if (!posTags.isEmpty()) {
			List<POS> right = JCasUtil.selectFollowing(view, POS.class, posTags.get(0), 1);
			if (!right.isEmpty()) {
				return new Feature(FEATURE_NAME, right.get(0).toString().split("\\n|\\r")[0]).asSet();
			}
		}
		return new Feature(FEATURE_NAME, "X").asSet();
	}

}
