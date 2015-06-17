/*!
 * PENTAHO CORPORATION PROPRIETARY AND CONFIDENTIAL
 *
 * Copyright 2002 - 2015 Pentaho Corporation (Pentaho). All rights reserved.
 *
 * NOTICE: All information including source code contained herein is, and
 * remains the sole property of Pentaho and its licensors. The intellectual
 * and technical concepts contained herein are proprietary and confidential
 * to, and are trade secrets of Pentaho and may be covered by U.S. and foreign
 * patents, or patents in process, and are protected by trade secret and
 * copyright laws. The receipt or possession of this source code and/or related
 * information does not convey or imply any rights to reproduce, disclose or
 * distribute its contents, or to manufacture, use, or sell anything that it
 * may describe, in whole or in part. Any reproduction, modification, distribution,
 * or public display of this information without the express written authorization
 * from Pentaho is strictly prohibited and in violation of applicable laws and
 * international treaties. Access to the source code contained herein is strictly
 * prohibited to anyone except those individuals and entities who have executed
 * confidentiality and non-disclosure agreements or other agreements with Pentaho,
 * explicitly covering such access.
 */

package ninja.mattburgess.metaverse.gremlin;

import com.google.common.collect.Lists;
import org.pentaho.dictionary.DictionaryConst;
import org.pentaho.metaverse.api.IDocument;
import org.pentaho.metaverse.api.Namespace;
import org.pentaho.metaverse.client.LineageClient;
import org.pentaho.metaverse.graph.LineageGraphMap;
import ninja.mattburgess.metaverse.gremlin.event.GremlinConsoleEvent;
import ninja.mattburgess.metaverse.gremlin.event.GremlinConsoleEventListener;
import ninja.mattburgess.metaverse.gremlin.views.GraphCanvasComposite;
import ninja.mattburgess.metaverse.gremlin.views.GraphElementTableComposite;
import ninja.mattburgess.metaverse.gremlin.views.PropertiesTableComposite;
import org.pentaho.metaverse.util.MetaverseUtil;
import com.tinkerpop.blueprints.Graph;
import com.tinkerpop.blueprints.Vertex;
import com.tinkerpop.blueprints.impls.tg.TinkerGraph;
import com.tinkerpop.gremlin.Imports;
import com.tinkerpop.gremlin.groovy.Gremlin;
import com.tinkerpop.gremlin.java.GremlinPipeline;
import com.tinkerpop.pipes.transform.ToStringPipe;
import com.tinkerpop.pipes.util.iterators.SingleIterator;
import groovy.lang.Binding;
import groovy.lang.Closure;
import groovy.ui.SystemOutputInterceptor;
import org.codehaus.groovy.tools.shell.Interpreter;
import org.codehaus.groovy.tools.shell.ParseCode;
import org.codehaus.groovy.tools.shell.ParseStatus;
import org.codehaus.groovy.tools.shell.Parser;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.custom.StackLayout;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.ShellAdapter;
import org.eclipse.swt.events.ShellEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.pentaho.di.cluster.SlaveConnectionManager;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.KettleVariablesList;
import org.pentaho.di.core.Props;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.logging.LoggingObjectInterface;
import org.pentaho.di.core.logging.LoggingObjectType;
import org.pentaho.di.core.logging.SimpleLoggingObject;
import org.pentaho.di.core.plugins.PluginRegistry;
import org.pentaho.di.core.variables.Variables;
import org.pentaho.di.core.vfs.KettleVFS;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.ui.core.PropsUI;
import org.pentaho.di.ui.core.gui.GUIResource;
import org.pentaho.di.ui.core.gui.WindowProperty;
import org.pentaho.di.ui.core.widget.StyledTextComp;
import org.pentaho.di.ui.spoon.Spoon;
import org.pentaho.di.ui.trans.step.BaseStepDialog;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Future;

public class GremlinConsoleDialog extends Dialog implements GremlinConsoleEventListener {

  public static final LoggingObjectInterface loggingObject = new SimpleLoggingObject(
    "Pentaho Lineage Console", LoggingObjectType.SPOON, null );

  private static final Logger log = LoggerFactory.getLogger( GremlinConsoleDialog.class );
  private List<String> IMPORTS;
  private List<String> DEFAULT_IMPORTS = Arrays.asList(
    "import com.tinkerpop.gremlin.Tokens.T",
    "import com.tinkerpop.gremlin.groovy.*",
    "import groovy.grape.Grape",
    "import org.pentaho.metaverse.api.*",
    "import org.pentaho.metaverse.api.analyzer.kettle.step.*",
    "import org.pentaho.metaverse.client.*",
    "import org.pentaho.metaverse.graph.*",
    "import org.pentaho.metaverse.impl.*",
    "import org.pentaho.metaverse.locator.*",
    "import org.pentaho.metaverse.util.*",
    "import org.pentaho.metaverse.service.*",
    "import org.pentaho.metaverse.analyzer.kettle.*",
    "import org.pentaho.metaverse.analyzer.kettle.step.*",
    "import org.pentaho.metaverse.analyzer.kettle.jobentry.*",
    "import org.pentaho.dictionary.*",
    "import org.pentaho.di.cluster.*",
    "import org.pentaho.di.core.*",
    "import org.pentaho.di.core.database.*",
    "import org.pentaho.di.core.exception.*",
    "import org.pentaho.di.core.extension.*",
    "import org.pentaho.di.core.gui.*",
    "import org.pentaho.di.core.logging.*",
    "import org.pentaho.di.core.plugins.*",
    "import org.pentaho.di.core.row.*",
    "import org.pentaho.di.core.variables.*",
    "import org.pentaho.di.core.vfs.*",
    "import org.pentaho.di.job.*",
    "import org.pentaho.di.repository.*",
    "import org.pentaho.di.trans.*",
    "import org.pentaho.di.trans.step.*",
    "import org.pentaho.di.ui.spoon.*",
    "import org.pentaho.di.ui.spoon.delegates.*",
    "import org.pentaho.di.ui.spoon.trans.*",
    "import org.pentaho.groovy.ui.spoon.*",
    "import org.pentaho.groovy.ui.spoon.repo.*"
  );

  private final StringBuffer outputString = new StringBuffer(
    "         \\,,,/\n"
      + "         (o o)\n"
      + "-----oOOo-(_)-oOOo-----\n"
      + "Welcome to Pentaho Lineage Console!\n" );
  private static final String INPUT_PROMPT = "gremlin> ";
  private static final String RESULT_PROMPT = "==>";

  private Parser gremlinParser;
  private Interpreter gremlinInterp;
  private static final String HISTORY_FILE = ".gremlin_groovy_history";
  private History HISTORY;
  private final SysOutClosure sysOutClosure = new SysOutClosure( this );
  private final SystemOutputInterceptor sysOutInterceptor = new SystemOutputInterceptor( sysOutClosure, true );

  // GUI components
  private Button wExec;
  private FormData fdExec;
  private Listener lsExec;

  private Button wClear;
  private FormData fdClear;
  private Listener lsClear;

  private StyledTextComp wScript;
  private FormData fdScript;

  protected Text wInput;
  private FormData fdInput;

  SashForm mainSash;

  // Right side of window (viz)
  private Composite rightComposite;
  private Composite graphComponentStack;
  private Composite tabularComponentStack;

  // Graph composites (stack layout, will swap in and out based on context)
  StackLayout graphStackLayout = new StackLayout();
  private GraphCanvasComposite graphCanvas;
  private GraphCanvasComposite backupCanvas;


  // Table composites (stack layout, will swap in and out based on context)
  StackLayout tableStackLayout = new StackLayout();
  private PropertiesTableComposite propsComposite;
  private GraphElementTableComposite graphElementTableComposite;

  private Shell shell;
  private PropsUI props;


  public GremlinConsoleDialog( Shell shell ) {
    super( shell );
    this.shell = shell;
    this.props = PropsUI.getInstance();
  }

  public GremlinConsoleDialog( Shell shell, int style ) {
    super( shell, style );
    this.shell = shell;
    this.props = PropsUI.getInstance();
  }

  public String open() {

    Shell parent = getParent();
    Display display = parent.getDisplay();

    shell = new Shell( parent, SWT.DIALOG_TRIM | SWT.RESIZE | SWT.MAX | SWT.MIN );
    props.setLook( shell );
    shell.setImage( GUIResource.getInstance().getImageConnection() );

    FormLayout formLayout = new FormLayout();
    formLayout.marginWidth = Const.FORM_MARGIN;
    formLayout.marginHeight = Const.FORM_MARGIN;

    shell.setLayout( formLayout );
    shell.setText( "Pentaho Lineage Console 0.1" );

    Control lastControl = shell;

    int margin = Const.MARGIN;

    mainSash = new SashForm( shell, SWT.HORIZONTAL | SWT.BORDER | SWT.RESIZE );
    FormData fdMainSash = GremlinConsoleUtils.createFormData();
    mainSash.setLayoutData( fdMainSash );

    Composite leftComposite = new Composite( mainSash, SWT.NONE );
    leftComposite.setLayout( new FormLayout() );
    FormData fdLeftComp = GremlinConsoleUtils.createFormData();
    leftComposite.setLayoutData( fdLeftComp );

    // Script output text area - this is where script results show up
    wScript =
      new StyledTextComp(
        new Variables(), leftComposite, SWT.MULTI | SWT.LEFT | SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL, "" );
    wScript.setEditable( false );
    clear();
    props.setLook( wScript, Props.WIDGET_STYLE_FIXED );
    fdScript = new FormData();
    fdScript.left = new FormAttachment( 0, margin );
    fdScript.top = new FormAttachment( 0, margin );
    fdScript.right = new FormAttachment( 100, -margin );
    fdScript.bottom = new FormAttachment( 90, margin );
    wScript.setLayoutData( fdScript );
    lastControl = wScript;

    wClear = new Button( leftComposite, SWT.PUSH );
    wClear.setText( "Clear" );
    fdClear = new FormData();
    fdClear.top = new FormAttachment( lastControl, margin );
    fdClear.right = new FormAttachment( 100, -margin );
    fdClear.bottom = new FormAttachment( 100, -margin );
    wClear.setLayoutData( fdClear );
    shell.setDefaultButton( wClear );

    lsClear = new Listener() {
      public void handleEvent( Event e ) {
        clear();
      }
    };
    wClear.addListener( SWT.Selection, lsClear );


    // Add execute button
    wExec = new Button( leftComposite, SWT.PUSH );
    URL url = getClass().getResource( "/gremlin.png" );
    InputStream is = null;
    try {
      is = url.openStream();
      Image image = new Image( display, is );
      wExec.setImage( image );
    } catch ( Exception e ) {
      // Nada
    } finally {
      if ( is != null ) {
        try {
          is.close();
        } catch ( Exception e ) {
          // Nada
        }
      }
    }
    fdExec = new FormData();
    fdExec.top = new FormAttachment( lastControl, margin );
    fdExec.right = new FormAttachment( wClear, -margin );
    fdExec.bottom = new FormAttachment( 100, -margin );
    wExec.setLayoutData( fdExec );
    shell.setDefaultButton( wExec );

    lsExec = new Listener() {
      public void handleEvent( Event e ) {
        exec();
      }
    };
    wExec.addListener( SWT.Selection, lsExec );

    // Input text area - this is where scripts are entered
    wInput = new Text( leftComposite, SWT.BORDER );
    fdInput = new FormData();
    fdInput.left = new FormAttachment( 0, 0 );
    fdInput.top = new FormAttachment( lastControl, margin );
    fdInput.right = new FormAttachment( wExec, -margin );
    fdInput.bottom = new FormAttachment( 100, -margin );
    wInput.setLayoutData( fdInput );

    //lastControl = wInput;

    wScript.addModifyListener( new ModifyListener() {
      public void modifyText( ModifyEvent arg0 ) {
        StyledText styledText = wScript.getStyledText();
        styledText.setTopIndex( styledText.getLineCount() - 1 );
      }

    } );

    wInput.addKeyListener( new KeyAdapter() {

      public void keyReleased( KeyEvent event ) {
        if ( event.keyCode == SWT.CR || event.keyCode == SWT.KEYPAD_CR ) {
          exec();
        } else if ( event.keyCode == SWT.ARROW_UP ) {
          historyUp();

        } else if ( event.keyCode == SWT.ARROW_DOWN ) {
          historyDown();
        }
      }
    } );

    //----------------------------------------------------------------------------------------------
    // Right side of sash
    // Visualization area - show graphs, propery maps, etc.
    rightComposite = new Composite( mainSash, SWT.NONE );
    rightComposite.setLayout( new FormLayout() );
    rightComposite.setLayoutData( GremlinConsoleUtils.createFormData() );
    SashForm vizSash = new SashForm( rightComposite, SWT.VERTICAL | SWT.BORDER | SWT.RESIZE );
    vizSash.setLayout( new FormLayout() );
    FormData fdVizSash = GremlinConsoleUtils.createFormData();
    vizSash.setLayoutData( fdVizSash );

    // Lineage graphCanvas panels
    graphComponentStack = new Composite( vizSash, SWT.NONE );
    graphComponentStack.setLayout( graphStackLayout );

    // Add composites to graph stack
    graphCanvas = new GraphCanvasComposite( graphComponentStack, SWT.NONE );

    graphStackLayout.topControl = graphCanvas;

    // Set up StackLayout, this will allow us to swap tabular views
    tabularComponentStack = new Composite( vizSash, SWT.NONE );
    tabularComponentStack.setLayout( tableStackLayout );

    // Add composites to table stack
    graphElementTableComposite = new GraphElementTableComposite( tabularComponentStack, SWT.NONE );
    propsComposite = new PropertiesTableComposite( tabularComponentStack, SWT.NONE );

    // A graph will be the first thing to come up, so show the graph element table
    tableStackLayout.topControl = graphElementTableComposite;

    // Set up listeners for child components. This main dialog will propagate events back to the other children
    graphCanvas.addListener( this );
    propsComposite.addListener( this );
    graphElementTableComposite.addListener( this );

    // Build up default import list
    List<String> importPackages = Imports.getImports();
    IMPORTS = new ArrayList<>( importPackages.size() + DEFAULT_IMPORTS.size() );
    for ( String imp : importPackages ) {
      IMPORTS.add( "import " + imp );
    }
    for ( String defaultImport : DEFAULT_IMPORTS ) {
      IMPORTS.add( defaultImport );
    }
    Thread.currentThread().setContextClassLoader( this.getClass().getClassLoader() );
    HISTORY = new History( HISTORY_FILE );
    gremlinParser = new Parser();
    Binding binding = new Binding();
    // Set automatic bindings (history, singletons, etc.)
    binding.setVariable( "history", HISTORY );
    binding.setVariable( "spoon", Spoon.getInstance() );
    binding.setVariable( "pluginRegistry", PluginRegistry.getInstance() );
    binding.setVariable( "kettleVFS", KettleVFS.getInstance() );
    binding.setVariable( "slaveConnectionManager", SlaveConnectionManager.getInstance() );
    binding.setVariable( "defaultVarMap", KettleVariablesList.getInstance().getDefaultValueMap() );
    binding.setVariable( "defaultVarDescMap", KettleVariablesList.getInstance().getDescriptionMap() );
    binding.setVariable( "client", new LineageClient() );
    binding.setVariable( "v", null );
    TransMeta tm = Spoon.getInstance().getActiveTransformation();
    if ( tm != null ) {
      binding.setVariable( "tm", tm );
    }
    gremlinInterp = new Interpreter( Thread.currentThread().getContextClassLoader(), binding );

    Gremlin.load();

    if ( !initializeShellWithScript( "metaverse-pdi.groovy", gremlinInterp ) ) {
      writeErrorToConsole( "Error during initialization, helper methods will not be available" );
    }

    // Try to get current graph, and put it in the Graph Canvas if available
    try {
      if ( tm != null ) {
        IDocument doc = MetaverseUtil.createDocument(
          new Namespace( "SPOON" ),
          tm, tm.getFilename(), tm.getName(), "ktr",
          URLConnection.getFileNameMap().getContentTypeFor( tm.getFilename() )
        );

        MetaverseUtil.addLineageGraph( doc, new TinkerGraph() );
        // addLineageGraph creates a Future, but we want it now so go get it :)
        Future<Graph> fg = LineageGraphMap.getInstance().get( tm );
        Graph g = fg.get();
        gremlinInterp.getContext().setVariable( "g", g );
        // Also set the selected vertex if one exists
        try {
          StepMeta step = Spoon.getInstance().getActiveTransGraph().getCurrentStep();
          if ( step != null && step.getName() != null ) {
            String name = step.getName();
            Iterable<Vertex> steps = g.getVertices( DictionaryConst.PROPERTY_TYPE, DictionaryConst.NODE_TYPE_TRANS_STEP );
            for ( Vertex vertex : steps ) {
              if ( name.equals( vertex.getProperty( DictionaryConst.PROPERTY_NAME ) ) ) {
                gremlinInterp.getContext().setVariable( "v", vertex );
                break;
              }
            }
          }
        } catch ( Throwable t ) {
          // No worries, we tried
        }
        renderResult( this, g );
      }

    } catch ( Throwable t ) {
      ByteArrayOutputStream baos = new ByteArrayOutputStream();
      t.printStackTrace( new PrintStream( baos ) );
      gremlinInterp.getContext().setVariable( "lastError", new String( baos.toByteArray() ) );
      writeErrorToConsole( t.getLocalizedMessage() + "\n" );
    }

    wInput.setFocus();

    BaseStepDialog.setSize( shell );

    // Detect X or ALT-F4 or something that kills this window...
    shell.addShellListener( new ShellAdapter() {
      public void shellClosed( ShellEvent e ) {
        close();
      }
    } );

    shell.open();
    while ( !shell.isDisposed() ) {
      if ( !display.readAndDispatch() ) {
        display.sleep();
      }
    }
    return "OK";
  }

  public void dispose() {
    HISTORY.save();
    gremlinInterp = null;
    props.setScreen( new WindowProperty( shell ) );
    graphCanvas.dispose();
    shell.dispose();
  }

  private void exec() {

    String scriptText = wInput.getText();
    setInputText( "" );
    if ( !Const.isEmpty( scriptText ) ) {

      try {
        wScript.getStyledText().append( INPUT_PROMPT + scriptText + "\n" );
        sysOutInterceptor.start();
        List<String> buff = Lists.newArrayList( IMPORTS );
        buff.add( scriptText );

        // Make sure it's valid Groovy
        parse( buff );

        // Now check for built-in shell commands (like clear)
        if ( scriptText.equalsIgnoreCase( "clear" ) ) {
          clear();
        } else if ( scriptText.equalsIgnoreCase( "exit" ) ) {
          HISTORY.save();
          close();
        } else {

          Object result = gremlinInterp.evaluate( buff );
          StringBuilder resultBuffer = new StringBuilder();
          List results = ( new GremlinPipeline( result ) ).toList();
          renderResults( this, results );

          ToStringPipe toStringPipe = new ToStringPipe();
          toStringPipe.setStarts( new SingleIterator<Object>( results ) );
          while ( toStringPipe.hasNext() ) {
            String resultString = toStringPipe.next().toString();
            resultBuffer.append( RESULT_PROMPT );
            resultBuffer.append( ( resultString == null ) ? "null" : resultString );
            resultBuffer.append( "\n" );
          }
          sysOutInterceptor.stop();
          String sysOut = sysOutClosure.getSysOut();
          if ( !Const.isEmpty( sysOut ) ) {
            wScript.getStyledText().append( sysOut + "\n" );
          }
          wScript.getStyledText().append( resultBuffer.toString() );
        }

      } catch ( Throwable t ) {
        gremlinInterp.getContext().setVariable( "lastError", t );
        writeErrorToConsole( t.getLocalizedMessage() + "\n" );
      }
      // Add this command to the history
      HISTORY.add( scriptText );
    }
  }

  private void parse( List<String> scriptBuffer ) throws Throwable {
    ParseStatus status = gremlinParser.parse( scriptBuffer );
    if ( status.getCode() != ParseCode.getCOMPLETE() ) {

      if ( ParseCode.getERROR().equals( status.getCode() ) ) {
        throw status.getCause();
      }

      throw new KettleException( "ParseError: Not a complete statement" );
    }
  }

  private void writeErrorToConsole( String errorText ) {
    int lastPos = wScript.getText().length() - 1;
    wScript.getStyledText().append( errorText );
    // Error text = bold and red
    StyleRange styleRange = new StyleRange();
    styleRange.start = lastPos;
    styleRange.length = errorText.length();
    styleRange.fontStyle = SWT.BOLD;
    styleRange.foreground = new Color( shell.getDisplay(), 255, 0, 0 );
    wScript.getStyledText().setStyleRange( styleRange );
  }

  private boolean initializeShellWithScript( final String initScriptFile, final Interpreter groovy ) {
    if ( initScriptFile != null ) {
      String line = "";
      List<String> buff = Lists.newArrayList( IMPORTS );

      try {
        final BufferedReader reader = new BufferedReader( new InputStreamReader(
          this.getClass().getClassLoader().getResourceAsStream( initScriptFile ), Charset.forName( "UTF-8" ) ) );
        while ( ( line = reader.readLine() ) != null ) {
          // We're not parsing this first -- make sure it's aight
          buff.add( line );
        }
        reader.close();
        Object result = gremlinInterp.evaluate( buff );
        return true;
      } catch ( FileNotFoundException fnfe ) {
        log.error( String.format( "Gremlin initialization file not found at [%s].", initScriptFile ) );
      } catch ( Exception e ) {
        log.error( String.format( "Bad line in Gremlin initialization file at [%s].", line ), e );
      }
    }
    return false;
  }

  private void close() {
    dispose();
  }

  @Override
  public void handleGremlinConsoleEvent( GremlinConsoleEvent event ) {
    Object data = event.getData();
    // Call either the list processing method or the individual processing method, depending on what's given
    if ( data instanceof List ) {
      renderResults( event.getSource(), (List<?>) data );
    } else {
      renderResult( event.getSource(), data, true );
    }
  }

  private void clear() {
    String startText = outputString.toString();
    wScript.setText( startText );
    StyleRange styleRange = new StyleRange();
    styleRange.start = 0;
    styleRange.length = startText.length();
    styleRange.fontStyle = SWT.BOLD;
    styleRange.foreground = new Color( shell.getDisplay(), 0, 0, 200 );
    wScript.getStyledText().setStyleRange( styleRange );
  }

  private void historyUp() {
    String lastCommand = HISTORY.up();
    if ( lastCommand != null ) {
      setInputText( lastCommand );
    }
  }

  private void historyDown() {
    String nextCommand = HISTORY.down();
    if ( nextCommand != null ) {
      setInputText( nextCommand );
    }
  }

  private void setInputText( String text ) {
    wInput.setText( text );
    // Advance the cursor
    wInput.setSelection( wInput.getText().length() );
  }

  private void renderResults( Object source, List<?> resultList ) {
    if ( resultList != null ) {
      // Special single-result processing (more efficient than overwriting output panels for all results)
      Object o = resultList.get( 0 );
      if ( resultList.size() == 1 ) {
        renderResult( source, o, true );
      } else {
        // TODO lists of things
      }
      refreshWidgets();
    }
  }

  private void renderResult( Object source, Object o ) {
    renderResult( source, o, true );
  }

  private void renderResult( Object source, Object o, boolean redraw ) {
    Graph g = graphCanvas.getGraph();

    // If it's a graphCanvas, draw it on the canvas
    if ( o instanceof Graph ) {
      if ( source != graphCanvas && graphCanvas.getGraph() != o ) {
        graphCanvas.clear();
      }
      graphCanvas.setData( o );
      // Bring the graph table viewer to the front
      tableStackLayout.topControl = graphElementTableComposite;
      graphElementTableComposite.setData( o );

    } else if ( o instanceof Vertex ) {
      Vertex v = (Vertex) o;
      if ( source == graphElementTableComposite ) {
        graphCanvas.setSelectedVertex( v );
      } else {
        graphCanvas.setData( o );

      }
      // Bring the vertex properties view out front
      tableStackLayout.topControl = propsComposite;
      propsComposite.setData( o );

      // Store this as variable "v" in the interpreter
      gremlinInterp.getContext().setVariable( "v", v );

      // Select the step in the Spoon canvas
      if ( DictionaryConst.NODE_TYPE_TRANS_STEP.equals( v.getProperty( DictionaryConst.PROPERTY_TYPE ) ) ) {
        String vertexName = v.getProperty( DictionaryConst.PROPERTY_NAME ).toString();
        TransMeta tm = Spoon.getInstance().getActiveTransformation();
        List<StepMeta> steps = tm.getSteps();
        if ( steps != null ) {
          for ( StepMeta step : steps ) {
            step.setSelected( vertexName.equals( step.getName() ) );
          }
        }
        Spoon.getInstance().getActiveTransGraph().redraw();
      }
    }

    if ( redraw ) {
      refreshWidgets();
    }

  }

  private void refreshWidgets() {
    graphStackLayout.topControl.redraw();
    graphComponentStack.layout( true );
    tableStackLayout.topControl.redraw();
    tabularComponentStack.layout( true );
    shell.layout( true );
  }

  private static class SysOutClosure extends Closure {

    StringBuffer sysOut = new StringBuffer( "" );

    public SysOutClosure( final Object owner ) {
      super( owner );
    }

    public Object call( final Object[] args ) {
      if ( args != null && args.length > 0 ) {
        sysOut.append( args[0].toString() );
      }
      return false;
    }

    public String getSysOut() {
      String result = sysOut.toString();
      sysOut = new StringBuffer( "" );
      return result;
    }
  }

}
