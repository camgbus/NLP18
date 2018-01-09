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
 * Extracts the chunk tag from the token two places after.
 */
public class RightSecNeighborChunk extends FeatureExtractorResource_ImplBase implements FeatureExtractor{

    public static final String FEATURE_NAME = "RightSecChunk";

	public Set<Feature> extract(JCas view, TextClassificationTarget target) throws TextClassificationException {
		List<Chunk> posTags = JCasUtil.selectCovered(view, Chunk.class, target);
		if (!posTags.isEmpty()) {
			List<Chunk> right = JCasUtil.selectFollowing(view, Chunk.class, posTags.get(0), 2);
			if (right.size()>1) {
				return new Feature(FEATURE_NAME, right.get(1).toString().split("\\n|\\r")[0]).asSet();
			}
		}
		return new Feature(FEATURE_NAME, "X").asSet();
	}

}
