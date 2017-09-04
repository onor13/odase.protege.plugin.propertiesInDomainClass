package odase.propertiesInDomainClass.hierarchyProviders;

import odase.propertiesInDomainClass.Tools;
import org.protege.editor.owl.OWLEditorKit;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.reasoner.BufferingMode;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import org.semanticweb.owlapi.reasoner.SimpleConfiguration;
import org.semanticweb.owlapi.reasoner.structural.StructuralReasoner;

import javax.annotation.Nonnull;
import java.util.*;

/**
 * Created by vblagodarov on 01-06-17.
 */
public abstract class InDomainOWLPropertiesProvider<P extends OWLProperty> {
    private OWLEditorKit editorKit;
    private Map<OWLPropertyExpression, OWLClassExpression> domains;

    public InDomainOWLPropertiesProvider(OWLEditorKit owlEditorKit) {
        editorKit = owlEditorKit;
        setOntology(editorKit.getOWLModelManager().getActiveOntology());
    }

    protected abstract Set<? extends OWLPropertyDomainAxiom<? extends OWLPropertyExpression>> getAxioms(OWLOntology ontology);

    public abstract boolean isValidTypePropertyExpression(OWLPropertyExpression pe);

    protected abstract Set<P> getAllProperties(OWLPropertyExpression pe);

    public Set<P> getProperties(OWLClass owlClass) {
        Set<P> resultSet = new HashSet<>();
        OWLReasoner reasoner = getReasoner();
        for (OWLPropertyExpression pe : domains.keySet()) {
            if (isInDomainClass(owlClass, pe, reasoner)) {
                resultSet.addAll(getAllProperties(pe));
            }
        }
        return resultSet;
    }

    public boolean isInDomainClass(OWLClass owlClass, OWLPropertyExpression pe, OWLReasoner reasoner) {
        OWLClassExpression ce = domains.get(pe);
        return ce == null ? false : ((reasoner.getSubClasses(ce, false).containsEntity(owlClass) ||
                reasoner.getEquivalentClasses(ce).contains(owlClass)) &&
                isValidTypePropertyExpression(pe));
    }

    public boolean isInDomainClass(OWLClass owlClass, OWLPropertyExpression pe) {
        return isInDomainClass(owlClass, pe, getReasoner());
    }

    public void setOntology(OWLOntology ontology) {
        initialize(getAxioms(ontology));
    }

    public void setOntologies(Collection<OWLOntology> ontologies) {
        Set<OWLPropertyDomainAxiom<? extends OWLPropertyExpression>> axioms = new HashSet<>();
        ontologies.forEach(ontology -> axioms.addAll(getAxioms(ontology)));
        initialize(axioms);
    }

    public void initialize(Set<? extends OWLPropertyDomainAxiom<? extends OWLPropertyExpression>> axioms) {
        domains = new HashMap<>();
        for (OWLPropertyDomainAxiom<? extends OWLPropertyExpression> axiom : axioms) {
            OWLPropertyExpression pe = axiom.getProperty();
            if (!domains.containsKey(pe)) {
                domains.put(pe, axiom.getDomain());
            } else {
                Set<OWLClassExpression> set = new HashSet<>(domains.get(pe).asConjunctSet());
                set.add(axiom.getDomain());
                domains.put(pe, editorKit.getOWLModelManager().getOWLDataFactory().getOWLObjectIntersectionOf(set));
            }
        }
    }

    protected OWLReasoner getReasoner() {
        OWLReasoner reasoner = editorKit.getModelManager().getOWLReasonerManager().getCurrentReasoner();
        if (!Tools.isReasonerActive(editorKit.getOWLModelManager().getOWLReasonerManager().getReasonerStatus())) {
            reasoner = new StructuralReasoner(editorKit.getModelManager().getActiveOntology(), new SimpleConfiguration(), BufferingMode.NON_BUFFERING);
        }
        return reasoner;
    }
}
