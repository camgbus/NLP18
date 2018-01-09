package extractors;

import java.util.Set;

import org.apache.uima.jcas.JCas;
import org.dkpro.tc.api.exception.TextClassificationException;
import org.dkpro.tc.api.features.Feature;
import org.dkpro.tc.api.features.FeatureExtractor;
import org.dkpro.tc.api.features.FeatureExtractorResource_ImplBase;
import org.dkpro.tc.api.type.TextClassificationTarget;

/**
 * Determines whether more than one character is upper case.
 */
public class MoreUppers extends FeatureExtractorResource_ImplBase
implements FeatureExtractor{
    public static final String FEATURE_NAME = "MoreUppers";

	public Set<Feature> extract(JCas view, TextClassificationTarget target) throws TextClassificationException {
		String token = target.getCoveredText();
		boolean oneUpper = false;
		for (int i=0; i<token.length(); i++){
			if (Character.isUpperCase(token.charAt(i))){
				if(!oneUpper) {
					oneUpper = true;
				} else return new Feature(FEATURE_NAME, true).asSet();
			}
		}
		return new Feature(FEATURE_NAME, false).asSet();
	}

}
