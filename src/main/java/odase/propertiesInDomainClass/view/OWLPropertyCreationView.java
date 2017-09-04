package odase.propertiesInDomainClass.view;

import org.protege.editor.owl.OWLEditorKit;
import org.protege.editor.owl.ui.rename.RenameEntityPanel;
import org.semanticweb.owlapi.formats.PrefixDocumentFormat;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.util.OWLEntityRenamer;
import org.semanticweb.owlapi.vocab.Namespaces;

import javax.annotation.Nonnull;
import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.List;

/**
 * Created by vblagodarov on 30-05-17.
 */
public abstract class OWLPropertyCreationView extends JFrame {
    protected OWLEditorKit owlEditorKit;
    protected JTextField nameField;
    protected OWLClass selectedClass;
    protected String uriPrefix= "";
    private static JLabel errorMsg = new JLabel("Entity already exists");
    private Box box = new Box(BoxLayout.Y_AXIS);
    IRI generatedIRI;

    public OWLPropertyCreationView(@Nonnull OWLEditorKit editorKit, OWLClass owlClass, Dimension dim, Point location)
    {
        super();

        owlEditorKit = editorKit;
        selectedClass = owlClass;
        OWLDocumentFormat format = owlEditorKit.getOWLModelManager().getOWLOntologyManager().getOntologyFormat(getOntology());
        System.out.println("OWLEditorKit ID: "+owlEditorKit.getId());
        if(format !=null){
            PrefixDocumentFormat prefixFormat = format.asPrefixOWLOntologyFormat();
            uriPrefix = prefixFormat.getDefaultPrefix();
        }

        generatedIRI = getUnique();

        //TODO: set the maximal height of nameField
        nameField = new JTextField(generatedIRI.toString());
        nameField.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) {
                isValidPropertyUri();
            }

            public void removeUpdate(DocumentEvent e) {
                isValidPropertyUri();
            }

            public void changedUpdate(DocumentEvent e) {
                isValidPropertyUri();
            }
        });
        hideErrorMsg();

        setSize(dim);
        setPreferredSize(dim);

        getContentPane().setLayout(new BorderLayout());
        ((JComponent)getContentPane()).setBorder(BorderFactory.createEtchedBorder());
        setLocation(location);

        this.addWindowListener(new WindowAdapter(){
            public void windowClosing(WindowEvent e){
                System.out.println("Closing New Property Windows");
                OWLEntityRenamer owlEntityRenamer = new OWLEntityRenamer(editorKit.getOWLModelManager().getOWLOntologyManager(), editorKit.getOWLModelManager().getActiveOntologies());
                IRI iri;
                if(isValidPropertyUri())
                {
                    iri=getCurrent();
                }
                else{
                    iri = generatedIRI;
                }
                System.out.println("Last Uri: "+iri.getShortForm());
                if(iri != null) {
                    List changes;
                    if (RenameEntityPanel.isAutoRenamePuns()) {
                        changes = owlEntityRenamer.changeIRI(getOwlProperty().getIRI(), iri);
                    } else {
                        changes = owlEntityRenamer.changeIRI(getOwlProperty(), iri);
                    }
                    editorKit.getOWLModelManager().applyChanges(changes);
                }
            }
        });
    }


    public void initializeAndDisplayView(){
        setTitle(frameTitle());
        addComponent(nameField, 1);
        addComponent(errorMsg);

        addComponents();

        getContentPane().add(box);
        setVisible(true);
        requestFocus();
    }

    protected abstract void addComponents();

    protected abstract OWLProperty getOwlProperty();

    protected abstract String getNewPropertyName();

    protected abstract String frameTitle();

    protected void addComponent(JComponent component){
        box.add(component);
        box.add(Box.createVerticalStrut(5));
    }

    protected void addComponent(Component component){
        box.add(component);
        box.add(Box.createVerticalStrut(5));
    }

    protected void addComponent(JComponent component, int separationHeight){
        box.add(component);
        box.add(Box.createVerticalStrut(separationHeight));
    }

    private IRI getUnique()
    {
        IRI iri = IRI.create(uriPrefix,getNewPropertyName());
        while (isIRIAlreadyUsed(iri)){
            iri = IRI.create(uriPrefix,getNewPropertyName());
        }
        return iri;
    }

    private IRI getCurrent(){
        return IRI.create(getEntityName());
    }

    private boolean isValidPropertyUri(){
        if(isIRIAlreadyUsed(getCurrent())){
            errorMsg.setForeground(Color.red);
            return false;
        }
        else{
            hideErrorMsg();
        }
        return true;
    }

    private void hideErrorMsg(){
        errorMsg.setForeground(this.getBackground());
    }

    private boolean isIRIAlreadyUsed(IRI iri) {
        if(iri.equals(generatedIRI)){
            return false;
        }
        for (OWLOntology ont : owlEditorKit.getModelManager().getOntologies()){
            if (ont.containsEntityInSignature(iri)){
                System.out.println("IRI "+iri.toString()+ " is already used");
                return true;
            }
        }
        return false;
    }


    protected OWLOntology getOntology(){
        return owlEditorKit.getModelManager().getActiveOntology();
    }

    protected OWLDataFactory getDataFactory(){
        return owlEditorKit.getModelManager().getOWLDataFactory();
    }

    public String getEntityName() {
        return nameField.getText().trim();
    }


    protected IRI getRawIRI() {
        return getRawIRI(getEntityName());
    }

    protected IRI getRawIRI(String text) {
        OWLOntology activeOntology = owlEditorKit.getModelManager().getActiveOntology();
        OWLOntologyManager manager = owlEditorKit.getModelManager().getOWLOntologyManager();
        OWLDocumentFormat format = manager.getOntologyFormat(activeOntology);
        for (Namespaces ns : Namespaces.values()) {
            if (text.startsWith(ns.name().toLowerCase() + ":")) {
                return IRI.create(ns.toString() + text.substring(ns.name().length() + 1));
            }
        }
        int colonIndex = text.indexOf(':');
        if (colonIndex >= 0 && format != null && format.isPrefixOWLOntologyFormat()) {
            PrefixDocumentFormat prefixes = format.asPrefixOWLOntologyFormat();
            String prefixName = text.substring(0, colonIndex + 1);
            String prefix = prefixes.getPrefix(prefixName);
            if (prefix != null) {
                return IRI.create(prefix + text.substring(colonIndex + 1));
            }
        }
        return IRI.create(text);
    }

    protected interface PropertyCharacteristicSetter {
        public OWLAxiom getAxiom();
    }
}
