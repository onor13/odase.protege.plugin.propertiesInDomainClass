package odase.propertiesInDomainClass.hierarchyProviders;

import org.protege.editor.owl.OWLEditorKit;
import org.protege.editor.owl.model.hierarchy.AbstractOWLPropertyHierarchyProvider;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.search.EntitySearcher;

import java.util.*;

import static java.util.stream.Collectors.toList;

/**
 * Created by vblagodarov on 01-06-17.
 */
public class SelectedClassOWLObjectPropertiesProvider extends BaseOWLPropertyHierarchyProvider<OWLObjectPropertyExpression, OWLObjectProperty> {

    OWLEditorKit editorKit;
    OWLClass owlClass;
    InDomainOWLObjectPropertiesProvider objectPropertiesProvider;

    public SelectedClassOWLObjectPropertiesProvider(OWLClass selectedClass, OWLEditorKit owlEditorKit) {
        super(owlEditorKit.getOWLModelManager().getOWLOntologyManager());
        editorKit = owlEditorKit;
        owlClass = selectedClass;
        objectPropertiesProvider = new InDomainOWLObjectPropertiesProvider(editorKit);
    }


    @Override
    protected boolean containsReference(OWLOntology ont, OWLObjectProperty prop) {
        return ont.containsObjectPropertyInSignature(prop.getIRI());
    }


    /**
     * Gets the relevant properties for the selected class in the specified ontology that are contained
     * within the property hierarchy.
     *
     * @param ont The ontology
     */
    protected Set<OWLObjectProperty> getReferencedProperties(OWLOntology ont) {
        objectPropertiesProvider.setOntology(ont);
        return objectPropertiesProvider.getProperties(owlClass);
    }

    @Override
    protected OWLObjectProperty getRoot() {
        return editorKit.getOWLModelManager().getOWLDataFactory().getOWLTopObjectProperty();
    }

    @Override
    protected Collection<OWLObjectProperty> getSuperProperties(OWLObjectProperty subProperty, Set<OWLOntology> ontologies) {
        objectPropertiesProvider.setOntologies(ontologies);
        Set<OWLObjectProperty> allValidProperties = objectPropertiesProvider.getProperties(owlClass);
        return EntitySearcher.getSuperProperties(subProperty, ontologies)
                .stream()
                .filter(p -> !p.isAnonymous() && allValidProperties.contains(p))
                .map(p -> (OWLObjectProperty) p)
                .collect(toList());
    }

    @Override
    protected Collection<OWLObjectProperty> getSubProperties(OWLObjectProperty superProp, Set<OWLOntology> ontologies) {
        List<OWLObjectProperty> result = new ArrayList<>();
        objectPropertiesProvider.setOntologies(ontologies);
        Set<OWLObjectProperty> allValidProperties = objectPropertiesProvider.getProperties(owlClass);
        for (OWLOntology ont : ontologies) {
            ont.getObjectSubPropertyAxiomsForSuperProperty(superProp)
                    .stream()
                    .map(OWLSubPropertyAxiom::getSubProperty)
                    .filter(p -> !p.isAnonymous() && allValidProperties.contains(p))
                    .map(p -> (OWLObjectProperty) p)
                    .forEach(result::add);
        }
        return result;
    }
}
