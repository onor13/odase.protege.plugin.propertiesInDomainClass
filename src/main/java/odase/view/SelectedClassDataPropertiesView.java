package odase.view;

import odase.hierarchyProviders.SelectedClassOWLDataPropertiesProvider;
import org.protege.editor.owl.model.entity.OWLEntityCreationSet;
import org.protege.editor.owl.model.hierarchy.AbstractOWLPropertyHierarchyProvider;
import org.protege.editor.owl.ui.OWLIcons;
import org.semanticweb.owlapi.model.*;

import javax.swing.*;

public class SelectedClassDataPropertiesView extends SelectedClassPropertiesView <OWLDataProperty> {

    private final Icon addDataPropertyIcon =  OWLIcons.getIcon("property.data.addsib.png");
    private final Icon addDataSubPropertyIcon =  OWLIcons.getIcon("property.data.addsub.png");
    private final Icon deleteDataPropertyIcon = OWLIcons.getIcon("property.data.delete.png");

    protected OWLSubPropertyAxiom getSubPropertyAxiom(OWLDataProperty child, OWLDataProperty parent) {
        return getOWLDataFactory().getOWLSubDataPropertyOfAxiom(child, parent);
    }

    @Override
    protected OWLAxiom addPropertyToDomain(OWLDataProperty dataProperty, OWLClass owlClass) {
        return  getOWLEditorKit().getModelManager().getOWLDataFactory().getOWLDataPropertyDomainAxiom(dataProperty, owlClass);
    }

    @Override
    protected OWLDataProperty topProperty() {
        return getOWLEditorKit().getOWLModelManager().getOWLDataFactory().getOWLTopDataProperty();
    }

    @Override
    protected OWLEntityCreationSet<OWLDataProperty> createProperty() {
        return getOWLWorkspace().createOWLDataProperty();
    }

    @Override
    protected Icon getAddPropertyIcon() {
        return addDataPropertyIcon;
    }

    @Override
    protected Icon getAddSubPropertyIcon() {
        return addDataSubPropertyIcon;
    }

    @Override
    protected Icon getDeletePropertyIcon() {
        return deleteDataPropertyIcon;
    }

    @Override
    protected String getAddPropertyToolTip() {
        return "Add Sibling Data Property";
    }

    @Override
    protected String getAddSubPropertyToolTip() {
        return "Add Data Sub Property";
    }

    @Override
    protected String getDeletePropertyToolTip() {
        return "Delete Data Property";
    }

    @Override
    protected AbstractOWLPropertyHierarchyProvider getNewPropertiesProvider(OWLClass owlClass) {
        return new SelectedClassOWLDataPropertiesProvider(owlClass, getOWLEditorKit());
    }
}
