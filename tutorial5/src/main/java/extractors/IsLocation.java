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

import type.Location;


/**
 * Determines whether there is a point character in the token.
 */
public class IsLocation extends FeatureExtractorResource_ImplBase
implements FeatureExtractor{
    public static final String FEATURE_NAME = "Location";

	public Set<Feature> extract(JCas view, TextClassificationTarget target) throws TextClassificationException {
		List<Location> l = JCasUtil.selectCovered(view, Location.class, target);
		if (l.isEmpty()) {
			return new Feature(FEATURE_NAME, false).asSet();
		} else {
			return new Feature(FEATURE_NAME, true).asSet();
		}
	}

}
