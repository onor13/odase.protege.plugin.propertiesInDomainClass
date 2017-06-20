package odase.hierarchyProviders;

import org.protege.editor.owl.OWLEditorKit;
import org.protege.editor.owl.model.inference.ReasonerStatus;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.reasoner.BufferingMode;
import org.semanticweb.owlapi.reasoner.NodeSet;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import org.semanticweb.owlapi.reasoner.SimpleConfiguration;
import org.semanticweb.owlapi.reasoner.structural.StructuralReasoner;
import org.semanticweb.owlapi.search.EntitySearcher;

import javax.annotation.Nonnull;
import java.util.*;

import static java.util.stream.Collectors.toList;
import static org.semanticweb.owlapi.apibinding.OWLFunctionalSyntaxFactory.ObjectIntersectionOf;

/**
 * Created by vblagodarov on 01-06-17.
 */
public class OWLMatchingPropertiesProvider<T extends OWLPropertyDomainAxiom<OWLPropertyExpression>>
{
    private OWLEditorKit editorKit;
    private OWLReasoner reasoner;
    private Map<OWLPropertyExpression, OWLClassExpression> domains;

    public OWLMatchingPropertiesProvider(@Nonnull Set<T> propertyAxioms, OWLEditorKit owlEditorKit)
    {
        editorKit = owlEditorKit;
        reasoner = editorKit.getModelManager().getOWLReasonerManager().getCurrentReasoner();
        if(!editorKit.getOWLModelManager().getOWLReasonerManager().getReasonerStatus().equals(ReasonerStatus.INITIALIZED)){
            reasoner = new StructuralReasoner(editorKit.getModelManager().getActiveOntology(), new SimpleConfiguration(), BufferingMode.NON_BUFFERING);
        }

        domains = new HashMap<>();
        for (T axiom: propertyAxioms) {
            OWLPropertyExpression pe=axiom.getProperty();
            if(!domains.containsKey(pe)) {
                domains.put(pe, axiom.getDomain());
            } else {
                Set<OWLClassExpression> set = new HashSet<>(domains.get(pe).asConjunctSet());
                set.add(axiom.getDomain());
                domains.put(pe, editorKit.getOWLModelManager().getOWLDataFactory().getOWLObjectIntersectionOf(set));
            }
        }
    }

    public Set<OWLObjectProperty> getObjectProperties(OWLClass owlClass)
    {
        Set<OWLObjectProperty> resultSet= new HashSet<>();
        for(OWLPropertyExpression p : domains.keySet()) {
            if((reasoner.getSubClasses(domains.get(p), false).containsEntity(owlClass) ||
                    reasoner.getEquivalentClasses(domains.get(p)).contains(owlClass)) && p.isObjectPropertyExpression()) {
                resultSet.addAll(p.getObjectPropertiesInSignature());
            }
        }
        return resultSet;
    }


    public Set<OWLDataProperty> getDataProperties(OWLClass owlClass)
    {
        Set<OWLDataProperty> resultSet= new HashSet<>();
        for(OWLPropertyExpression p : domains.keySet()) {
            if((reasoner.getSubClasses(domains.get(p), false).containsEntity(owlClass) ||
                    reasoner.getEquivalentClasses(domains.get(p)).contains(owlClass)) && p.isDataPropertyExpression()) {
                resultSet.addAll(p.getDataPropertiesInSignature());
            }
        }
        return resultSet;
    }
}
