package odase.propertiesInDomainClass.hierarchyProviders;

import org.protege.editor.owl.OWLEditorKit;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.model.parameters.Imports;

import java.util.Set;

/**
 * Created by vblagodarov on 04-09-17.
 */
public class InDomainOWLObjectPropertiesProvider extends InDomainOWLPropertiesProvider<OWLObjectProperty> {

    public InDomainOWLObjectPropertiesProvider(OWLEditorKit owlEditorKit) {
        super(owlEditorKit);
    }

    @Override
    protected Set<? extends OWLPropertyDomainAxiom<? extends OWLPropertyExpression>> getAxioms(OWLOntology ontology) {
        return ontology.getAxioms(AxiomType.OBJECT_PROPERTY_DOMAIN, Imports.INCLUDED);
    }

    @Override
    public boolean isValidTypePropertyExpression(OWLPropertyExpression pe) {
        return pe.isObjectPropertyExpression();
    }

    @Override
    public Set<OWLObjectProperty> getAllProperties(OWLPropertyExpression pe) {
        return pe.getObjectPropertiesInSignature();
    }
}
