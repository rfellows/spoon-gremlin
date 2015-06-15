package ninja.mattburgess.metaverse.gremlin.views;

import org.pentaho.dictionary.DictionaryConst;
import ninja.mattburgess.metaverse.gremlin.GremlinConsoleUtils;
import com.tinkerpop.blueprints.Graph;
import com.tinkerpop.blueprints.Vertex;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.TableItem;
import org.pentaho.di.core.Props;
import org.pentaho.di.core.variables.Variables;
import org.pentaho.di.ui.core.PropsUI;
import org.pentaho.di.ui.core.widget.ColumnInfo;
import org.pentaho.di.ui.core.widget.TableView;


/**
 * Created by mburgess on 5/18/15.
 */
public class GraphElementTableComposite extends BaseGremlinConsoleComposite {

  PropsUI props;
  final TableView elementTable;
  Graph graph;

  public GraphElementTableComposite( Composite parent, int style ) {
    super( parent, style );
    props = PropsUI.getInstance();
    setLayout( new FormLayout() );
    setLayoutData( GremlinConsoleUtils.createFormData() );

    // Property table panel
    ColumnInfo[] propCols =
      new ColumnInfo[]{
        new ColumnInfo(
          "Name",
          ColumnInfo.COLUMN_TYPE_TEXT, false, true ),
        new ColumnInfo(
          "Value",
          ColumnInfo.COLUMN_TYPE_TEXT, false, true ),
      };

    ModifyListener lsm = new ModifyListener() {

      @Override
      public void modifyText( ModifyEvent modifyEvent ) {
        String name = elementTable.getItem( elementTable.getSelectionIndex() )[1];
        Vertex foundVertex = graph.getVertices( "name", name ).iterator().next();
        notifyListeners( foundVertex );
      }
    };

    elementTable = new TableView( new Variables(), this,
      SWT.BORDER | SWT.FULL_SELECTION | SWT.SINGLE, propCols, 1, lsm, props );
    FormData fdProps = GremlinConsoleUtils.createFormData();
    elementTable.setLayoutData( fdProps );


    props.setLook( elementTable, Props.WIDGET_STYLE_TABLE );

    elementTable.table.addSelectionListener( new SelectionAdapter() {
      public void widgetSelected( SelectionEvent e ) {
        TableItem row = elementTable.table.getItem( elementTable.getSelectionIndex() );
        String name = row.getText( 1 );
        Vertex foundVertex = graph.getVertices( "name", name ).iterator().next();
        notifyListeners( foundVertex );
      }
    } );
  }

  @Override
  public void setData( Object o ) {

    if ( o instanceof Graph ) {
      graph = (Graph) o;

      elementTable.setVisible( false );
      elementTable.removeAll();


      Iterable<Vertex> vertices = graph.getVertices();
      if ( vertices != null ) {
        int index = 0;
        for ( Vertex v : vertices ) {

          TableItem item = new TableItem( elementTable.table, SWT.NONE );

          Object name = v.getProperty( DictionaryConst.PROPERTY_NAME );
          Object type = v.getProperty( DictionaryConst.PROPERTY_TYPE );

          item.setText( 0, Integer.toString( ++index ) );
          item.setText( 1, ( name == null ? "" : name.toString() ) );
          item.setText( 2, ( type == null ? "" : type.toString() ) );
        }
      }
      elementTable.removeEmptyRows();
      elementTable.setVisible( true );
      //elementTable.layout();
      //elementTable.pack();
    }
  }
}
