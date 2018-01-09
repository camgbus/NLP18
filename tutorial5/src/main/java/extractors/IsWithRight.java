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

import type.Beginning;
import type.Chunk;

/**
 * Extracts whether the token is with the same construct as the following token.
 * This is considered to occur when the token has the same chunk annotation as
 * the neighbor, and the neighbor does not have a "beginning" annotation.
 */
public class IsWithRight 
extends FeatureExtractorResource_ImplBase
implements FeatureExtractor{
	
    public static final String FEATURE_NAME = "WithRight";

	public Set<Feature> extract(JCas view, TextClassificationTarget target) throws TextClassificationException {
		List<Chunk> chunkTags = JCasUtil.selectCovered(view, Chunk.class, target);
		List<TextClassificationTarget> rightTarget = JCasUtil.selectFollowing(view, TextClassificationTarget.class, target, 1);
		if (!rightTarget.isEmpty()) {
			if (JCasUtil.selectCovered(view, Beginning.class, rightTarget.get(0)).isEmpty()) {
				List<Chunk> right = JCasUtil.selectFollowing(view, Chunk.class, chunkTags.get(0), 1);
				if (right.get(0).getClass().equals(chunkTags.get(0).getClass())){
					return new Feature(FEATURE_NAME, true).asSet();
				}
			}
		}
		return new Feature(FEATURE_NAME, false).asSet();
	}

}
