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
 * Extracts the chunk tag from the preceding token.
 */
public class LeftNeighborChunk extends FeatureExtractorResource_ImplBase implements FeatureExtractor{

    public static final String FEATURE_NAME = "LeftChunk";

	public Set<Feature> extract(JCas view, TextClassificationTarget target) throws TextClassificationException {
		List<Chunk> posTags = JCasUtil.selectCovered(view, Chunk.class, target);
		if (posTags.size()>0) {
			List<Chunk> left = JCasUtil.selectPreceding(view, Chunk.class, posTags.get(0), 1);
			if (!left.isEmpty()) {
				return new Feature(FEATURE_NAME, left.get(0).toString().split("\\n|\\r")[0]).asSet();
			}
		}
		return new Feature(FEATURE_NAME, "X").asSet();
	}

}
