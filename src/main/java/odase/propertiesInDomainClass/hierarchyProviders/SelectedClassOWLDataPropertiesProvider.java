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
public class SelectedClassOWLDataPropertiesProvider extends BaseOWLPropertyHierarchyProvider<OWLDataPropertyExpression, OWLDataProperty> {

    OWLClass owlClass;
    OWLEditorKit editorKit;
    InDomainOWLDataPropertiesProvider dataPropertiesProvider;

    public SelectedClassOWLDataPropertiesProvider(OWLClass selectedClass, OWLEditorKit owlEditorKit) {
        super(owlEditorKit.getOWLModelManager().getOWLOntologyManager());
        owlClass = selectedClass;
        editorKit = owlEditorKit;
        dataPropertiesProvider = new InDomainOWLDataPropertiesProvider(owlEditorKit);
    }

    /**
     * Gets the relevant properties in the specified ontology that are contained
     * within the property hierarchy.  For example, for an object property hierarchy
     * this would constitute the set of referenced object properties in the specified
     * ontology.
     *
     * @param ont The ontology
     */
    @Override
    protected Set<? extends OWLDataProperty> getReferencedProperties(OWLOntology ont) {
        dataPropertiesProvider.setOntology(ont);
        return dataPropertiesProvider.getProperties(owlClass);
    }

    protected boolean containsReference(OWLOntology ont, OWLDataProperty prop) {
        return ont.containsDataPropertyInSignature(prop.getIRI());
    }

    protected OWLDataProperty getRoot() {
        return getManager().getOWLDataFactory().getOWLTopDataProperty();
    }

    @Override
    protected Collection<OWLDataProperty> getSuperProperties(OWLDataProperty subProperty, Set<OWLOntology> ontologies) {
        dataPropertiesProvider.setOntologies(ontologies);
        Set<OWLDataProperty> allValidProperties = dataPropertiesProvider.getProperties(owlClass);
        return EntitySearcher.getSuperProperties(subProperty, ontologies)
                .stream()
                .filter(p -> !p.isAnonymous() && allValidProperties.contains(p))
                .map(p -> (OWLDataProperty) p)
                .collect(toList());
    }

    @Override
    protected Collection<OWLDataProperty> getSubProperties(OWLDataProperty superProp, Set<OWLOntology> ontologies) {
        List<OWLDataProperty> result = new ArrayList<>();
        dataPropertiesProvider.setOntologies(ontologies);
        Set<OWLDataProperty> allValidProperties = dataPropertiesProvider.getProperties(owlClass);
        for (OWLOntology ont : ontologies) {
            ont.getDataSubPropertyAxiomsForSuperProperty(superProp)
                    .stream()
                    .map(OWLSubPropertyAxiom::getSubProperty)
                    .filter(p -> !p.isAnonymous() && allValidProperties.contains(p))
                    .map(p -> (OWLDataProperty) p)
                    .forEach(result::add);
        }
        return result;
    }
}
