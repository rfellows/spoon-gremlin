transMeta = { it ? spoon?.findTransformation(it) : spoon?.activeTransformation }

preview = { it ?: spoon?.previewFile() }

Gremlin.defineStep('steps', [Vertex, Pipe],
  { name ->
    _().has('type', DictionaryConst.NODE_TYPE_TRANS_STEP)
  }
)
Gremlin.defineStep('step', [Vertex, Pipe],
  { name ->
    _().steps().has('name', name)
  }
)
Gremlin.defineStep('myTrans', [Vertex, Pipe],
  {
    _().in.loop(1) { it.loops < 10 } { it.object.type == 'Transformation' }
  }
)
Gremlin.defineStep('creator', [Vertex, Pipe],
  { field ->
    _().in('hops_to').loop(1) { it.loops < 10 } {
      it.object != null
    }.as('step').out('creates').has('name', field).back("step")
  }
)
Gremlin.defineStep('origin', [Vertex, Pipe],
  { transName, stepName ->
    _().and(_().has("name", stepName), _().in("contains").and(_().has("name", transName), _().has("type", "Transformation")))
  }
)

g = new TinkerGraph()
dc = MetaverseBeanUtil.instance.get('IDocumentController')
gw = new GraphMLWriter()
gson = new GraphSONReader()
gsonw = new GraphSONWriter()
g2 = new TinkerGraph()
gr = new GraphMLReader(g2)

// Helper constants (to save typing)
name = NAME = DictionaryConst.PROPERTY_NAME
type = TYPE = DictionaryConst.PROPERTY_TYPE
TRANS = DictionaryConst.NODE_TYPE_TRANS
STEP = DictionaryConst.NODE_TYPE_TRANS_STEP
FIELD = DictionaryConst.NODE_TYPE_TRANS_FIELD
JOB = DictionaryConst.NODE_TYPE_JOB
JOBENTRY = DictionaryConst.NODE_TYPE_JOB_ENTRY
NAME = DictionaryConst.PROPERTY_NAME
DERIVES = DictionaryConst.LINK_DERIVES
wtf = WTF = 'I know right?'

getPathFormatter = {
  def x = [{ "${it.name} (${it.type})" }];
  10.times { x << { "<-[ ${it.label} ]-" }; x << { "${it.name} (${it.type})" } }; x.toArray() as Closure[]
}
getRevPathFormatter = {
  def x = [{ "${it.name} (${it.type})" }];
  10.times { x << { "-[ ${it.label} ]->" }; x << { "${it.name} (${it.type})" } }; x.toArray() as Closure[]
}

loadGraph = { fname ->
  new File(fname).withInputStream { i -> gr.inputGraph(i) }
}

saveGraph = { fname ->
  new File(fname).withOutputStream { i -> gw.outputGraph(g, i) }
}

step = { name ->
  g.V(NAME, name).has(TYPE, STEP)
}

field = { name ->
  g.V(NAME, name).has(TYPE, FIELD)
}

// Measures the number of seconds it takes to run the given closure
timeExec = { closureToMeasure ->
  now = System.currentTimeMillis()
  closureToMeasure()
  then = System.currentTimeMillis()
  "${(then - now) / 1000.0f} s"
}

ls = { dir ->
  new File(dir ?: '.').list()
}

preview = {
  spoon.activeTransformation ? spoon.previewTransformation() : "No active transformation to preview"
}

help ="""Usage: TBD"""
