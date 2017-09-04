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
public class SelectedClassOWLObjectPropertiesProvider extends AbstractOWLPropertyHierarchyProvider<OWLClassExpression, OWLObjectPropertyExpression, OWLObjectProperty> {

    OWLEditorKit editorKit;
    OWLClass owlClass;
    public SelectedClassOWLObjectPropertiesProvider(OWLClass selectedClass, OWLEditorKit owlEditorKit) {
        super(owlEditorKit.getOWLModelManager().getOWLOntologyManager());
        editorKit = owlEditorKit;
        owlClass = selectedClass;
    }


    @Override
    protected Set getPropertiesReferencedInChange(List<? extends OWLOntologyChange> changes) {
        Set<OWLObjectProperty> properties = new HashSet<>();
        for (OWLOntologyChange change : changes) {
            if (change.isAxiomChange()) {
                OWLAxiomChange axiomChange = (OWLAxiomChange) change;
                for (OWLEntity entity : axiomChange.getSignature()) {
                    if (entity.isOWLObjectProperty()) {
                        properties.add(entity.asOWLObjectProperty());
                    }
                }
            }
        }
        return properties;
    }

    @Override
    protected boolean containsReference(OWLOntology ont, OWLObjectProperty prop) {
        return ont.containsObjectPropertyInSignature(prop.getIRI());
    }


    /**
     * Gets the relevant properties for the selected class in the specified ontology that are contained
     * within the property hierarchy.
     * @param ont The ontology
     */
    protected Set<OWLObjectProperty> getReferencedProperties(OWLOntology ont) {
        return new OWLMatchingPropertiesProvider(ont.getAxioms(AxiomType.OBJECT_PROPERTY_DOMAIN), editorKit).getObjectProperties(owlClass);
    }

    @Override
    protected Set<? extends OWLSubPropertyAxiom> getSubPropertyAxiomForRHS(OWLObjectProperty prop, OWLOntology ont) {
        return ont.getObjectSubPropertyAxiomsForSuperProperty(prop);
    }


    @Override
    protected OWLObjectProperty getRoot() {
        return editorKit.getOWLModelManager().getOWLDataFactory().getOWLTopObjectProperty();
    }

    @Override
    protected Collection<OWLObjectProperty> getSuperProperties(OWLObjectProperty subProperty, Set<OWLOntology> ontologies) {
        //TODO: make it better
        Set<OWLAxiom> objectPropDomain = new HashSet<>();
        for(OWLOntology ont : ontologies) {
            ont.getAxioms(AxiomType.OBJECT_PROPERTY_DOMAIN)
                    .stream()
                    .forEach(objectPropDomain::add);
        }
        Set<OWLObjectProperty> allValidProperties=new OWLMatchingPropertiesProvider(objectPropDomain, editorKit).getObjectProperties(owlClass);
        return EntitySearcher.getSuperProperties(subProperty, ontologies)
                .stream()
                .filter(p -> !p.isAnonymous() && allValidProperties.contains(p))
                .map(p -> (OWLObjectProperty) p)
                .collect(toList());
    }

    @Override
    protected Collection<OWLObjectProperty> getSubProperties(OWLObjectProperty superProp, Set<OWLOntology> ontologies) {
        //TODO: make it better
        List<OWLObjectProperty> result = new ArrayList<>();
        Set<OWLAxiom> objectPropDomain = new HashSet<>();
        for(OWLOntology ont : ontologies) {
            ont.getAxioms(AxiomType.OBJECT_PROPERTY_DOMAIN)
                    .stream()
                    .forEach(objectPropDomain::add);
        }
        Set<OWLObjectProperty> allValidProperties=new OWLMatchingPropertiesProvider(objectPropDomain, editorKit).getObjectProperties(owlClass);
        for(OWLOntology ont : ontologies) {
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
