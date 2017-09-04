package odase.propertiesInDomainClass.view;

import odase.propertiesInDomainClass.hierarchyProviders.SelectedClassOWLObjectPropertiesProvider;
import org.protege.editor.owl.model.entity.OWLEntityCreationSet;
import org.protege.editor.owl.model.hierarchy.AbstractOWLPropertyHierarchyProvider;
import org.protege.editor.owl.ui.OWLIcons;
import org.semanticweb.owlapi.model.*;

import javax.swing.*;

public class SelectedClassObjectPropertiesView  extends SelectedClassPropertiesView <OWLObjectProperty>{

    private final Icon addObjectPropertyIcon = OWLIcons.getIcon("property.object.addsib.png");
    private final Icon addObjectSubPropertyIcon = OWLIcons.getIcon("property.object.addsub.png");
    private final Icon deleteObjectPropertyIcon = OWLIcons.getIcon("property.object.delete.png");

    protected OWLSubPropertyAxiom getObjectSubPropertyAxiom(OWLObjectProperty child, OWLObjectProperty parent) {
        return getOWLDataFactory().getOWLSubObjectPropertyOfAxiom(child, parent);
    }

    @Override
    protected OWLEntityCreationSet<OWLObjectProperty> createProperty() {
        return getOWLWorkspace().createOWLObjectProperty();
    }

    @Override
    protected OWLSubPropertyAxiom getSubPropertyAxiom(OWLObjectProperty child, OWLObjectProperty parent) {
        return getOWLDataFactory().getOWLSubObjectPropertyOfAxiom(child, parent);
    }

    @Override
    protected OWLAxiom addPropertyToDomain(OWLObjectProperty objectProperty, OWLClass owlClass) {
        return getOWLEditorKit().getModelManager().getOWLDataFactory().getOWLObjectPropertyDomainAxiom(objectProperty, owlClass);
    }

    @Override
    protected OWLObjectProperty topProperty() {
        return getOWLEditorKit().getOWLModelManager().getOWLDataFactory().getOWLTopObjectProperty();
    }

    @Override
    protected Icon getAddPropertyIcon() {
        return addObjectPropertyIcon;
    }

    @Override
    protected Icon getAddSubPropertyIcon() {
        return addObjectSubPropertyIcon;
    }

    @Override
    protected Icon getDeletePropertyIcon() {
        return deleteObjectPropertyIcon;
    }

    @Override
    protected String getAddPropertyToolTip() {
        return "Add Sibling Object Property";
    }

    @Override
    protected String getAddSubPropertyToolTip() {
        return "Add Object Sub Property";
    }

    @Override
    protected String getDeletePropertyToolTip() {
        return "Delete Object Property";
    }

    @Override
    protected AbstractOWLPropertyHierarchyProvider getNewPropertiesProvider(OWLClass owlClass) {
        return new SelectedClassOWLObjectPropertiesProvider(owlClass, getOWLEditorKit());
    }
}
