package ninja.mattburgess.metaverse.gremlin.views;

import com.google.common.collect.Lists;
import org.pentaho.dictionary.DictionaryConst;
import ninja.mattburgess.metaverse.gremlin.GremlinConsoleUtils;
import com.tinkerpop.blueprints.Direction;
import com.tinkerpop.blueprints.Edge;
import com.tinkerpop.blueprints.Graph;
import com.tinkerpop.blueprints.Vertex;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DragDetectEvent;
import org.eclipse.swt.events.DragDetectListener;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.zest.core.widgets.GraphConnection;
import org.eclipse.zest.core.widgets.GraphItem;
import org.eclipse.zest.core.widgets.GraphNode;
import org.eclipse.zest.core.widgets.SpoonGremlinGraph;
import org.eclipse.zest.core.widgets.ZestStyles;
import org.eclipse.zest.layouts.LayoutAlgorithm;
import org.eclipse.zest.layouts.LayoutStyles;
import org.eclipse.zest.layouts.algorithms.CompositeLayoutAlgorithm;
import org.eclipse.zest.layouts.algorithms.DirectedGraphLayoutAlgorithm;
import org.eclipse.zest.layouts.algorithms.GridLayoutAlgorithm;
import org.eclipse.zest.layouts.algorithms.HorizontalLayoutAlgorithm;
import org.eclipse.zest.layouts.algorithms.HorizontalShift;
import org.eclipse.zest.layouts.algorithms.HorizontalTreeLayoutAlgorithm;
import org.eclipse.zest.layouts.algorithms.RadialLayoutAlgorithm;
import org.eclipse.zest.layouts.algorithms.SpringLayoutAlgorithm;
import org.eclipse.zest.layouts.algorithms.TreeLayoutAlgorithm;
import org.eclipse.zest.layouts.algorithms.VerticalLayoutAlgorithm;
import org.pentaho.di.core.Const;

import java.util.HashMap;
import java.util.List;

/**
 * Created by mburgess on 5/18/15.
 */
public class GraphCanvasComposite extends BaseGremlinConsoleComposite {

  private static final LayoutAlgorithm TREE_LAYOUT = new CompositeLayoutAlgorithm(
    LayoutStyles.NO_LAYOUT_NODE_RESIZING,
    new LayoutAlgorithm[]{
      new TreeLayoutAlgorithm( LayoutStyles.NO_LAYOUT_NODE_RESIZING ),
      new HorizontalShift( LayoutStyles.NO_LAYOUT_NODE_RESIZING )
    } );

  private static final LayoutAlgorithm HORIZ_TREE_LAYOUT = new CompositeLayoutAlgorithm(
    LayoutStyles.NO_LAYOUT_NODE_RESIZING,
    new LayoutAlgorithm[]{
      new HorizontalTreeLayoutAlgorithm( LayoutStyles.NO_LAYOUT_NODE_RESIZING ),
      new HorizontalShift( LayoutStyles.NO_LAYOUT_NODE_RESIZING )
    } );

  private static final LayoutAlgorithm HORIZ_LAYOUT = new CompositeLayoutAlgorithm(
    LayoutStyles.NO_LAYOUT_NODE_RESIZING,
    new LayoutAlgorithm[]{
      new HorizontalLayoutAlgorithm( LayoutStyles.NO_LAYOUT_NODE_RESIZING ),
      new HorizontalShift( LayoutStyles.NO_LAYOUT_NODE_RESIZING )
    } );

  private static final LayoutAlgorithm DIGRAPH_LAYOUT = new CompositeLayoutAlgorithm(
    LayoutStyles.NO_LAYOUT_NODE_RESIZING,
    new LayoutAlgorithm[]{
      new DirectedGraphLayoutAlgorithm( LayoutStyles.NO_LAYOUT_NODE_RESIZING ),
      new HorizontalShift( LayoutStyles.NO_LAYOUT_NODE_RESIZING )
    } );

  private static final LayoutAlgorithm SPRING_LAYOUT = new CompositeLayoutAlgorithm(
    LayoutStyles.NO_LAYOUT_NODE_RESIZING,
    new LayoutAlgorithm[]{
      new SpringLayoutAlgorithm( LayoutStyles.NO_LAYOUT_NODE_RESIZING ),
      new HorizontalShift( LayoutStyles.NO_LAYOUT_NODE_RESIZING )
    } );

  private static final LayoutAlgorithm RADIAL_LAYOUT = new CompositeLayoutAlgorithm(
    LayoutStyles.NO_LAYOUT_NODE_RESIZING,
    new LayoutAlgorithm[]{
      new RadialLayoutAlgorithm( LayoutStyles.NO_LAYOUT_NODE_RESIZING ),
      new HorizontalShift( LayoutStyles.NO_LAYOUT_NODE_RESIZING )
    } );

  private static final LayoutAlgorithm GRID_LAYOUT = new CompositeLayoutAlgorithm(
    LayoutStyles.NO_LAYOUT_NODE_RESIZING,
    new LayoutAlgorithm[]{
      new GridLayoutAlgorithm( LayoutStyles.NO_LAYOUT_NODE_RESIZING ),
      new HorizontalShift( LayoutStyles.NO_LAYOUT_NODE_RESIZING )
    } );

  private static final LayoutAlgorithm VERTICAL_LAYOUT = new CompositeLayoutAlgorithm(
    LayoutStyles.NO_LAYOUT_NODE_RESIZING,
    new LayoutAlgorithm[]{
      new VerticalLayoutAlgorithm( LayoutStyles.NO_LAYOUT_NODE_RESIZING ),
      new HorizontalShift( LayoutStyles.NO_LAYOUT_NODE_RESIZING )
    } );

  private org.eclipse.zest.core.widgets.Graph graphCanvas;
  private Menu popupMenu;
  private LayoutAlgorithm layoutAlgorithm = TREE_LAYOUT;
  private Graph graph;
  private Vertex selectedVertex = null;
  private HashMap<String, GraphNode> graphNodeMap = new HashMap<>();
  private boolean isDrag = false;
  private boolean updateGraph = true;

  private final SelectionAdapter selectionAdapter =
    new SelectionAdapter() {
      public void widgetSelected( SelectionEvent e ) {
        graphCanvas.setSelection( new GraphItem[]{ (GraphItem) e.item } );
      }
    };

  private final MouseAdapter mouseAdapter =
    new MouseAdapter() {
      @Override
      public void mouseUp( MouseEvent mouseEvent ) {
        updateGraph = true;
        List<GraphItem> selections = graphCanvas.getSelection();
        if ( Const.isEmpty( selections ) ) {
          if ( graph != null ) {
            // No widgets are selected, so the click is in the graph background. Let the main dialog know
            notifyListeners( graph );
          }
        } else if ( selections.get( 0 ) instanceof GraphNode ) {
          // If dragging, don't update the graph but update everything else. If not dragging you're clicking,
          // so update the graph and the table(s).
          if ( isDrag ) {
            updateGraph = false;
          }
          notifyListeners( selections.get( 0 ).getData() );
        }
        isDrag = false;
      }
    };

  public GraphCanvasComposite( Composite parent, int style ) {
    super( parent, style );
    setLayout( new FormLayout() );
    setLayoutData( GremlinConsoleUtils.createFormData() );
  }

  @Override
  public void setData( Object o ) {
    List<Vertex> validVertices;
    // Is this a new graph to display?
    if ( o instanceof Graph ) {
      if ( o == graph ) {
        List<GraphNode> nodes = graphCanvas.getNodes();
        for ( GraphNode node : nodes ) {
          node.setVisible( true );
          updateGraph = true;
        }
      } else {
        setGraph( (Graph) o );
        validVertices = Lists.newLinkedList( graph.getVertices() );
        if ( graph != null ) {
          for ( Vertex v : validVertices ) {
            if ( GremlinConsoleUtils.displayVertex( v ) ) {

              GremlinConsoleUtils.enhanceVertex( v );
              GraphNode gn = new GraphNode( graphCanvas, SWT.NONE, (String) v.getProperty( DictionaryConst.PROPERTY_NAME ) );
              gn.setData( v );
              String colorString = v.getProperty( DictionaryConst.PROPERTY_COLOR );
              int red = Integer.parseInt( colorString.substring( 1, 3 ), 16 );
              int green = Integer.parseInt( colorString.substring( 3, 5 ), 16 );
              int blue = Integer.parseInt( colorString.substring( 5, 7 ), 16 );

              gn.setBackgroundColor( new Color( getParent().getDisplay(), red, green, blue ) );
              graphNodeMap.put( (String) v.getId(), gn );
            }
          }
          for ( Edge e : graph.getEdges() ) {
            GremlinConsoleUtils.enhanceEdge( e );
            Vertex leftV = e.getVertex( Direction.OUT );
            GraphNode leftNode = graphNodeMap.get( leftV.getId() );

            Vertex rightV = e.getVertex( Direction.IN );
            GraphNode rightNode = graphNodeMap.get( rightV.getId() );

            if ( leftNode != null && rightNode != null ) {
              GraphConnection gc = new GraphConnection( graphCanvas, ZestStyles.CONNECTIONS_DIRECTED, leftNode, rightNode );
              gc.setText( e.getLabel() );
            }
          }
        }
      }
    } else if ( o instanceof Vertex && updateGraph ) {
      selectedVertex = (Vertex) o;
      validVertices = Lists.newLinkedList( selectedVertex.getVertices( Direction.BOTH ) );
      validVertices.add( selectedVertex );
      List<GraphNode> nodes = graphCanvas.getNodes();
      for ( GraphNode node : nodes ) {
        node.setVisible( false );
      }
      for ( Vertex v : validVertices ) {
        if ( GremlinConsoleUtils.displayVertex( v ) ) {
          String vName = (String) v.getId();
          if ( vName != null ) {
            GraphNode node = graphNodeMap.get( vName );
            if ( node != null ) {
              node.setVisible( true );
            }
          }
        }
      }
      // TODO temporarily set new layout for neighborhood view?
    }
    if ( updateGraph ) {
      setLayoutAlgorithm( layoutAlgorithm );
    }
    updateGraph = true;
  }


  private void setLayoutAlgorithm( LayoutAlgorithm la ) {
    layoutAlgorithm = la;
    graphCanvas.setLayoutAlgorithm( la, true );
  }

  private void createGraphCanvas() {
    graphCanvas = new SpoonGremlinGraph( this, SWT.NONE );

    graphCanvas.setNodeStyle( ZestStyles.IGNORE_INVISIBLE_LAYOUT );

    popupMenu = new Menu( this );

    MenuItem showAllItem = new MenuItem( popupMenu, SWT.NONE );
    showAllItem.setText( "Show All" );
    showAllItem.addSelectionListener( new SelectionAdapter() {
      @Override
      public void widgetSelected( SelectionEvent selectionEvent ) {
        setData( getGraph() );
        redraw();
      }
    } );

    MenuItem layoutItem = new MenuItem( popupMenu, SWT.CASCADE );
    layoutItem.setText( "Layout" );
    Menu layoutMenu = new Menu( popupMenu );
    layoutItem.setMenu( layoutMenu );

    // Add Layout algorithms as context-menu items
    MenuItem diGraphItem = new MenuItem( layoutMenu, SWT.NONE );
    diGraphItem.setText( "Directed Graph" );
    diGraphItem.addSelectionListener( new SelectionAdapter() {
      @Override
      public void widgetSelected( SelectionEvent selectionEvent ) {
        setLayoutAlgorithm( DIGRAPH_LAYOUT );
      }
    } );
    MenuItem gridItem = new MenuItem( layoutMenu, SWT.NONE );
    gridItem.setText( "Grid" );
    gridItem.addSelectionListener( new SelectionAdapter() {
      @Override
      public void widgetSelected( SelectionEvent selectionEvent ) {
        setLayoutAlgorithm( GRID_LAYOUT );
      }
    } );
    MenuItem horizItem = new MenuItem( layoutMenu, SWT.NONE );
    horizItem.setText( "Horizontal" );
    horizItem.addSelectionListener( new SelectionAdapter() {
      @Override
      public void widgetSelected( SelectionEvent selectionEvent ) {
        setLayoutAlgorithm( HORIZ_LAYOUT );
      }
    } );

    MenuItem horizTreeItem = new MenuItem( layoutMenu, SWT.NONE );
    horizTreeItem.setText( "Horizontal Tree" );
    horizTreeItem.addSelectionListener( new SelectionAdapter() {
      @Override
      public void widgetSelected( SelectionEvent selectionEvent ) {
        setLayoutAlgorithm( HORIZ_TREE_LAYOUT );
      }
    } );
    MenuItem radialItem = new MenuItem( layoutMenu, SWT.NONE );
    radialItem.setText( "Radial" );
    radialItem.addSelectionListener( new SelectionAdapter() {
      @Override
      public void widgetSelected( SelectionEvent selectionEvent ) {
        setLayoutAlgorithm( RADIAL_LAYOUT );
      }
    } );
    MenuItem springItem = new MenuItem( layoutMenu, SWT.NONE );
    springItem.setText( "Spring" );
    springItem.addSelectionListener( new SelectionAdapter() {
      @Override
      public void widgetSelected( SelectionEvent selectionEvent ) {
        setLayoutAlgorithm( SPRING_LAYOUT );
      }
    } );
    MenuItem treeItem = new MenuItem( layoutMenu, SWT.NONE );
    treeItem.setText( "Tree" );
    treeItem.addSelectionListener( new SelectionAdapter() {
      @Override
      public void widgetSelected( SelectionEvent selectionEvent ) {
        setLayoutAlgorithm( TREE_LAYOUT );
      }
    } );
    MenuItem vertItem = new MenuItem( layoutMenu, SWT.NONE );
    vertItem.setText( "Vertical" );
    vertItem.addSelectionListener( new SelectionAdapter() {
      @Override
      public void widgetSelected( SelectionEvent selectionEvent ) {
        setLayoutAlgorithm( VERTICAL_LAYOUT );
      }
    } );

    graphCanvas.setMenu( popupMenu );
    graphCanvas.setLayout( new FormLayout() );
    graphCanvas.setLayoutData( GremlinConsoleUtils.createFormData() );
    graphCanvas.addSelectionListener( selectionAdapter );
    graphCanvas.addMouseListener( mouseAdapter );
    graphCanvas.addDragDetectListener( new DragDetectListener() {
      @Override
      public void dragDetected( DragDetectEvent dragDetectEvent ) {
        // If dragging, don't redraw the graph
        isDrag = true;
      }
    } );
    graphCanvas.redraw();
  }

  public Graph getGraph() {
    return graph;
  }

  public void setGraph( Graph graph ) {
    this.graph = graph;
    graphCanvas.redraw();
  }

  public void clear() {
    if ( graphCanvas != null ) {
      graphCanvas.dispose();
    }
    createGraphCanvas();
  }


  @Override
  public void dispose() {
    if ( popupMenu != null && !popupMenu.isDisposed() ) {
      popupMenu.dispose();
      popupMenu = null;
    }
    if ( graphCanvas != null && !graphCanvas.isDisposed() ) {
      graphCanvas.dispose();
      graphCanvas = null;
    }
    super.dispose();
  }

  public void setSelectedVertex( Vertex v ) {
    graphCanvas.setSelection( new GraphItem[]{ graphNodeMap.get( v.getId().toString() ) } );
  }
}
