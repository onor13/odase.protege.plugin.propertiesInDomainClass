package odase.hierarchyProviders;

import org.protege.editor.owl.OWLEditorKit;
import org.protege.editor.owl.model.hierarchy.AbstractOWLPropertyHierarchyProvider;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.search.EntitySearcher;

import java.util.*;

import static java.util.stream.Collectors.toList;

/**
 * Created by vblagodarov on 01-06-17.
 */
public class SelectedClassOWLDataPropertiesProvider  extends AbstractOWLPropertyHierarchyProvider<OWLDataRange, OWLDataPropertyExpression, OWLDataProperty> {

    OWLClass owlClass;
    OWLEditorKit editorKit;
    public SelectedClassOWLDataPropertiesProvider(OWLClass selectedClass, OWLEditorKit owlEditorKit) {
        super(owlEditorKit.getOWLModelManager().getOWLOntologyManager());
        owlClass = selectedClass;
        editorKit = owlEditorKit;
    }

    protected Set<OWLDataProperty> getPropertiesReferencedInChange(List<? extends OWLOntologyChange> changes) {
        Set<OWLDataProperty> result = new HashSet<>();
        for (OWLOntologyChange change : changes) {
            if (change.isAxiomChange()) {
                for (OWLEntity entity : change.getSignature()) {
                    if (entity.isOWLDataProperty()) {
                        result.add(entity.asOWLDataProperty());
                    }
                }
            }
        }
        return result;
    }


    /**
     * Gets the relevant properties in the specified ontology that are contained
     * within the property hierarchy.  For example, for an object property hierarchy
     * this would constitute the set of referenced object properties in the specified
     * ontology.
     * @param ont The ontology
     */
    @Override
    protected Set<? extends OWLDataProperty> getReferencedProperties(OWLOntology ont) {
        return new OWLMatchingPropertiesProvider(ont.getAxioms(AxiomType.DATA_PROPERTY_DOMAIN), editorKit).getDataProperties(owlClass);
    }

    protected boolean containsReference(OWLOntology ont, OWLDataProperty prop) {
        return ont.containsDataPropertyInSignature(prop.getIRI());
    }

    protected Set<? extends OWLSubPropertyAxiom<OWLDataPropertyExpression>> getSubPropertyAxiomForRHS(
            OWLDataProperty prop, OWLOntology ont) {
        return ont.getDataSubPropertyAxiomsForSuperProperty(prop);
    }

    protected OWLDataProperty getRoot() {
        return getManager().getOWLDataFactory().getOWLTopDataProperty();
    }

    @Override
    protected Collection<OWLDataProperty> getSuperProperties(OWLDataProperty subProperty, Set<OWLOntology> ontologies) {
        //TODO: make it better
        Set<OWLAxiom> dataPropDomain = new HashSet<>();
        for(OWLOntology ont : ontologies) {
            ont.getAxioms(AxiomType.DATA_PROPERTY_DOMAIN)
                    .stream()
                    .forEach(dataPropDomain::add);
        }
        Set<OWLDataProperty> allValidProperties=new OWLMatchingPropertiesProvider(dataPropDomain, editorKit).getDataProperties(owlClass);
        return EntitySearcher.getSuperProperties(subProperty, ontologies)
                .stream()
                .filter(p -> !p.isAnonymous() && allValidProperties.contains(p))
                .map(p -> (OWLDataProperty) p)
                .collect(toList());
    }

    @Override
    protected Collection<OWLDataProperty> getSubProperties(OWLDataProperty superProp, Set<OWLOntology> ontologies) {
        //TODO: make it better
        List<OWLDataProperty> result = new ArrayList<>();
        Set<OWLAxiom> dataPropDomain = new HashSet<>();
        for(OWLOntology ont : ontologies) {
            ont.getAxioms(AxiomType.DATA_PROPERTY_DOMAIN)
                    .stream()
                    .forEach(dataPropDomain::add);
        }
        Set<OWLDataProperty> allValidProperties=new OWLMatchingPropertiesProvider(dataPropDomain, editorKit).getDataProperties(owlClass);
        for(OWLOntology ont : ontologies) {
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
