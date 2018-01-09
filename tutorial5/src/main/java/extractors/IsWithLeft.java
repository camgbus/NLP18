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
 * Extracts whether the token is with the same construct as the preceding token.
 * This is considered to occur when the token has the same chunk annotation as
 * the neighbor, and the token does not have a "beginning" annotation.
 */
public class IsWithLeft 
extends FeatureExtractorResource_ImplBase
implements FeatureExtractor{
	
    public static final String FEATURE_NAME = "WithLeft";

	public Set<Feature> extract(JCas view, TextClassificationTarget target) throws TextClassificationException {
		List<Chunk> chunkTags = JCasUtil.selectCovered(view, Chunk.class, target);
		if (JCasUtil.selectCovered(view, Beginning.class, target).isEmpty()) {
			List<Chunk> left = JCasUtil.selectPreceding(view, Chunk.class, chunkTags.get(0), 1);
			if (!left.isEmpty()) {
				if (left.get(0).getClass().equals(chunkTags.get(0).getClass())){
					return new Feature(FEATURE_NAME, true).asSet();
				}
			}
		}
		return new Feature(FEATURE_NAME, false).asSet();
	}
}
