package ninja.mattburgess.metaverse.gremlin.views;

import ninja.mattburgess.metaverse.gremlin.GremlinConsoleUtils;
import com.tinkerpop.blueprints.Vertex;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.TableItem;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.variables.Variables;
import org.pentaho.di.ui.core.PropsUI;
import org.pentaho.di.ui.core.widget.ColumnInfo;
import org.pentaho.di.ui.core.widget.TableView;

/**
 * Created by mburgess on 5/18/15.
 */
public class PropertiesTableComposite extends BaseGremlinConsoleComposite {

  private TableView wProps;
  private PropsUI props;

  public PropertiesTableComposite( Composite parent, int style ) {
    super( parent, style );
    this.props = PropsUI.getInstance();
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
    wProps =
      new TableView( new Variables(), this, SWT.BORDER | SWT.FULL_SELECTION | SWT.SINGLE, propCols, 1, null, props );
    wProps.setReadonly( true );
    props.setLook( wProps );
    FormData fdProps = GremlinConsoleUtils.createFormData();
    wProps.setLayoutData( fdProps );
  }


  @Override
  public void setData( Object o ) {
    if ( o instanceof Vertex ) {
      Vertex v = (Vertex) o;
      wProps.clearAll( false );
      for ( String key : v.getPropertyKeys() ) {
        String value = Const.NVL( v.getProperty( key ).toString(), "<null>" );

        TableItem item = new TableItem( wProps.table, SWT.NONE );
        item.setText( 1, key );
        item.setText( 2, value );

      }
      wProps.removeEmptyRows();
      wProps.setRowNums();
      wProps.optWidth( true );
    }
  }

}
