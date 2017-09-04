package odase.propertiesInDomainClass;

import org.protege.editor.owl.OWLEditorKit;
import org.protege.editor.owl.model.inference.ReasonerStatus;

/**
 * Created by vblagodarov on 01-09-17.
 */
public class Tools {

    public static boolean isReasonerActive(ReasonerStatus status){
        return status.equals(ReasonerStatus.INITIALIZED) ||
                status.equals(ReasonerStatus.OUT_OF_SYNC) ||
                status.equals(ReasonerStatus.INCONSISTENT);
    }
}
