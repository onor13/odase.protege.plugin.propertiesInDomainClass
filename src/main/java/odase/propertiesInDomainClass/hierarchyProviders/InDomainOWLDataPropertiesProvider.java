package odase.propertiesInDomainClass.hierarchyProviders;

import org.protege.editor.owl.OWLEditorKit;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.model.parameters.Imports;
import java.util.Set;

/**
 * Created by vblagodarov on 04-09-17.
 */
public class InDomainOWLDataPropertiesProvider extends InDomainOWLPropertiesProvider<OWLDataProperty> {

    public InDomainOWLDataPropertiesProvider(OWLEditorKit owlEditorKit) {
        super(owlEditorKit);
    }


    @Override
    protected Set<? extends OWLPropertyDomainAxiom<? extends OWLPropertyExpression>> getAxioms(OWLOntology ontology) {
        return ontology.getAxioms(AxiomType.DATA_PROPERTY_DOMAIN, Imports.INCLUDED);
    }

    @Override
    public boolean isValidTypePropertyExpression(OWLPropertyExpression pe) {
        return pe.isDataPropertyExpression();
    }

    @Override
    public Set<OWLDataProperty> getAllProperties(OWLPropertyExpression pe) {
        return pe.getDataPropertiesInSignature();
    }

}
