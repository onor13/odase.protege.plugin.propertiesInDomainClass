package odase.propertiesInDomainClass.view;

import odase.propertiesInDomainClass.Tools;
import odase.propertiesInDomainClass.hierarchyProviders.BaseOWLPropertyHierarchyProvider;
import org.protege.editor.owl.OWLEditorKit;
import org.protege.editor.owl.model.OWLModelManager;
import org.protege.editor.owl.model.entity.OWLEntityCreationSet;
import org.protege.editor.owl.model.hierarchy.AbstractOWLPropertyHierarchyProvider;
import org.protege.editor.owl.ui.action.AbstractDeleteEntityAction;
import org.protege.editor.owl.ui.action.AbstractOWLTreeAction;
import org.protege.editor.owl.ui.tree.OWLModelManagerTree;
import org.protege.editor.owl.ui.tree.OWLObjectTree;
import org.protege.editor.owl.ui.tree.OWLObjectTreeCellRenderer;
import org.protege.editor.owl.ui.view.ChangeListenerMediator;
import org.protege.editor.owl.ui.view.cls.AbstractOWLClassViewComponent;
import org.semanticweb.owlapi.model.*;

import javax.swing.*;
import javax.swing.event.TreeSelectionListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.util.*;
import java.util.List;

/**
 * Created by vblagodarov on 01-06-17.
 */
public abstract class SelectedClassPropertiesView <T extends OWLProperty> extends AbstractOWLClassViewComponent {

    protected static final String ADD_GROUP = "A";
    protected static final String DELETE_GROUP = "B";
    protected static final String FIRST_SLOT = "A";
    protected static final String SECOND_SLOT = "B";

    private AbstractOWLTreeAction<T> addSiblingPropertyAction;
    private AbstractOWLTreeAction<T> addSubPropertyAction;
    private AbstractDeleteEntityAction<T> deletePropertyAction;
    private BaseOWLPropertyHierarchyProvider propertiesProvider;
    protected T userSelectedProperty;
    private JScrollPane scrollPane;

    private JTextField reasonerInfo;
    protected OWLObjectTree<T> tree;
    protected List<OWLClass> notAcceptableForSelection;
    private OWLClass userSelectedClass =null;
    protected ChangeListenerMediator changeListenerMediator;
    protected TreeSelectionListener listener = e -> transmitSelection();

    protected abstract Icon getAddPropertyIcon();
    protected abstract Icon getAddSubPropertyIcon();
    protected abstract Icon getDeletePropertyIcon();
    protected abstract String getAddPropertyToolTip();
    protected abstract String getAddSubPropertyToolTip();
    protected abstract String getDeletePropertyToolTip();
    protected abstract BaseOWLPropertyHierarchyProvider getNewPropertiesProvider(OWLClass selectedClass);
    protected abstract OWLEntityCreationSet<T> createProperty();
    protected abstract OWLSubPropertyAxiom getSubPropertyAxiom(T child, T parent);
    protected abstract OWLAxiom addPropertyToDomain(T property, OWLClass owlClass);
    protected abstract T topProperty();

    @Override
    protected OWLClass updateView(OWLClass selectedClass) {
        updateReasonerInfo();
        if(selectedClass ==null){
            return getOWLEditorKit().getModelManager().getOWLDataFactory().getOWLThing();
        }
        userSelectedClass = selectedClass;

        OWLOntology activeOntology=getOWLEditorKit().getModelManager().getActiveOntology();
        if(activeOntology ==null){
            return userSelectedClass;
        }
        propertiesProvider = getNewPropertiesProvider(userSelectedClass);
        propertiesProvider.setOntologies(getOWLEditorKit().getModelManager().getActiveOntologies());
        tree.removeTreeSelectionListener(listener);
        tree.removeAll();
        tree.dispose();
        tree = new OWLModelManagerTree<T>(getOWLEditorKit(), propertiesProvider);
        tree.setCellRenderer(new OWLObjectTreeCellRenderer(getOWLEditorKit()));
        tree.expandRow(0);

        changeListenerMediator = new ChangeListenerMediator();
        tree.addTreeSelectionListener(listener);
        scrollPane.setViewportView(tree);

        updateSelectedProperty(null);
        updateAddPropertyActions(isEnabledForAddAction());

        return selectedClass;
    }

    @Override
    public void disposeView() {
        tree.removeTreeSelectionListener(listener);
        tree.removeAll();
        tree.dispose();
    }

    @Override
    public void initialiseClassView() throws Exception {
        setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
        this.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                // TODO Auto-generated method stub
                super.componentResized(e);

            }
        });
        OWLEditorKit editorKit=getOWLEditorKit();
        OWLModelManager modelManager=editorKit.getModelManager();
        notAcceptableForSelection = new Vector<>(2);
        notAcceptableForSelection.add(modelManager.getOWLDataFactory().getOWLThing());
        notAcceptableForSelection.add(modelManager.getOWLDataFactory().getOWLNothing());

        propertiesProvider= getNewPropertiesProvider(null);
        tree = new OWLModelManagerTree<T>(editorKit, propertiesProvider);
        tree.expandRow(0);
        scrollPane = new JScrollPane(tree);

        reasonerInfo = new JTextField();
        reasonerInfo.setEditable(false);
        reasonerInfo.setMaximumSize(new Dimension(Integer.MAX_VALUE, reasonerInfo.getPreferredSize().height));
        updateReasonerInfo();

        add(scrollPane, BorderLayout.NORTH);
        add(reasonerInfo, BorderLayout.PAGE_END);

        changeListenerMediator = new ChangeListenerMediator();
        tree.addTreeSelectionListener(listener);
        performExtraInitialisation();
    }

    protected void performExtraInitialisation() throws Exception {
        addSiblingPropertyAction= new AbstractOWLTreeAction<T>(getAddPropertyToolTip(),
                getAddPropertyIcon(),
                tree.getSelectionModel()){

            public void actionPerformed(ActionEvent event) {
                createNewSibling();
            }
            protected boolean canPerform(T cls) {
                return isEnabledForAddAction();
            }
        };
        addSiblingPropertyAction.setEnabled(isEnabledForAddAction());
        addAction(addSiblingPropertyAction, ADD_GROUP, SECOND_SLOT);

        addSubPropertyAction=new AbstractOWLTreeAction<T>(getAddSubPropertyToolTip(),
                getAddSubPropertyIcon(),
                tree.getSelectionModel()) {
            public void actionPerformed(ActionEvent event) {
                createNewSubProperty();
            }
            protected boolean canPerform(T prop) {
                return anySelectedProperty();
            }
        };
        addSubPropertyAction.setEnabled(anySelectedProperty());
        addAction(addSubPropertyAction, ADD_GROUP, FIRST_SLOT);

        deletePropertyAction = new AbstractDeleteEntityAction<T>(getDeletePropertyToolTip(),
                getDeletePropertyIcon(),
                getOWLEditorKit(),
                propertiesProvider,
                () -> getSelected()) {
            //TODO: possibility to delete multiple at once, see OWLObjectTree
            protected String getPluralDescription() {
                return "properties";
            }

            @Override
            public void actionPerformed(ActionEvent e) {
                super.actionPerformed(e);
            }

            @Override
            public void updateState() {
                super.updateState();
                if(isEnabled()) {
                    setEnabled(isEnabledForDeleteAction());
                }
            }
        };

        deletePropertyAction.setEnabled(isEnabledForDeleteAction());
        addAction(deletePropertyAction, DELETE_GROUP, FIRST_SLOT);
    }

    private void updateReasonerInfo(){
        if(isReasonerActive()){
            String reasonerName = getReasonerName();
            if(reasonerName ==null){
                reasonerInfo.setText("reasoner is active");
            }
            else{
                reasonerInfo.setText("reasoner "+reasonerName+" is active");
            }

            reasonerInfo.setForeground(Color.black);
        }
        else{
            reasonerInfo.setText("using Structural reasoner, the view might be incomplete");
            reasonerInfo.setForeground(Color.red);
        }
    }


    protected Set<T> getSelected(){
        return new HashSet<T>()
        {{
            add(userSelectedProperty);
        }};
    }

    protected boolean isEnabledForAddAction(){
        if(userSelectedClass ==null){
            return false;
        }
        for(OWLClass oc: notAcceptableForSelection){
            if(oc.equals(userSelectedClass)){
                return false;
            }
        }
        return true;
    }

    protected T getSelectedProperty(){
        return tree.getSelectedOWLObject();
    }

    protected void setSelectedProperty(T prop){
        tree.setSelectedOWLObject(prop);
        transmitSelection();
    }

    protected boolean anySelectedProperty() {
        return getSelectedProperty() != null;
    }

    protected boolean isEnabledForDeleteAction(){
        OWLProperty selectedProperty= getSelectedProperty();
        if(selectedProperty !=null && selectedProperty != topProperty()){
            return true;
        }
        return false;
    }

    protected void updateSelectedProperty(T property){
        if(property == null){
            deletePropertyAction.setEnabled(false);
            addSubPropertyAction.setEnabled(false);
        }
        else{
            if(property.equals(topProperty())){
                deletePropertyAction.setEnabled(false);
            }
            else{
                deletePropertyAction.setEnabled(true);
            }
            addSubPropertyAction.setEnabled(true);
        }
        userSelectedProperty = property;
    }

    private boolean isReasonerActive(){
        return Tools.isReasonerActive(getOWLEditorKit().getOWLModelManager().getOWLReasonerManager().getReasonerStatus());

    }

    private String getReasonerName(){
        String name = getOWLEditorKit().getOWLModelManager().getOWLReasonerManager().getCurrentReasonerName();
        if(name == null){
            name=getOWLEditorKit().getOWLModelManager().getOWLReasonerManager().getCurrentReasoner().getReasonerName();
        }
        return name;
    }


    protected void updateAddPropertyActions(boolean value){
        addSiblingPropertyAction.setEnabled(value);
    }

    private void transmitSelection() {
        if (isSynchronizing()){
            updateSelectedProperty(getSelectedProperty());
            setGlobalSelection(userSelectedProperty);
        }
        changeListenerMediator.fireStateChanged(this);
    }

    public void createNewSibling() {
        T property = getSelectedProperty();
        if (property == null) {
            property = topProperty();
        }
        // We need to apply the changes in the active ontology
        OWLEntityCreationSet<T> creationSet = createProperty();
        if (creationSet != null) {
            // Combine the changes that are required to create the OWLAnnotationProperty, with the
            // changes that are required to make it a sibling property.
            List<OWLOntologyChange> changes = new ArrayList<>();
            changes.addAll(creationSet.getOntologyChanges());
            OWLOntology ont = getOWLModelManager().getActiveOntology();
            T prop=creationSet.getOWLEntity();
            changes.add(new AddAxiom(ont, addPropertyToDomain(prop, userSelectedClass)));

            Set<T> ps = propertiesProvider.getParents(property);
            for (T parentProperty : ps) {
                if (shouldAddAsParentOfNewlyCreatedProperty(parentProperty)) {
                    OWLAxiom ax = getSubPropertyAxiom(prop, parentProperty);
                    changes.add(new AddAxiom(ont, ax));
                }
            }
            getOWLModelManager().applyChanges(changes);
            setSelectedProperty(prop);
        }
    }

    public void createNewSubProperty(){
        T selectedProperty = getSelectedProperty();
        if (selectedProperty == null) {
            return;
        }
        OWLEntityCreationSet<T> set = createProperty();
        if (set != null) {
            T prop = set.getOWLEntity();
            java.util.List<OWLOntologyChange> changes = new ArrayList<>();
            changes.addAll(set.getOntologyChanges());
            changes.add(new AddAxiom(getOWLModelManager().getActiveOntology(), addPropertyToDomain(prop, userSelectedClass)));

            if (shouldAddAsParentOfNewlyCreatedProperty(selectedProperty)) {
                OWLAxiom ax = getSubPropertyAxiom(set.getOWLEntity(), selectedProperty);
                changes.add(new AddAxiom(getOWLModelManager().getActiveOntology(), ax));
            }
            getOWLModelManager().applyChanges(changes);
            setSelectedProperty(prop);
        }
    }

    protected boolean shouldAddAsParentOfNewlyCreatedProperty(T parent) {
        return !propertiesProvider.getRoots().contains(parent);
    }
}
