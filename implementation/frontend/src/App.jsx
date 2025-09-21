import React, { useCallback, useMemo, useRef, useState } from 'react'
import { ReactFlow, Background, Controls, MiniMap, addEdge, useEdgesState, useNodesState, Handle, Position, Panel } from '@xyflow/react'
import '@xyflow/react/dist/style.css'
import { Link as LinkIcon, Database as DatabaseIcon, Braces as BracesIcon, Play as PlayIcon, Trash2 } from 'lucide-react'

/** @typedef {"httpAction"|"dbAction"|"transformAction"} NodeKind */
const DRAG_TYPE = 'application/x-node-type'

function Button({ children, onClick, variant='default', ...props }){
  const styles = variant==='danger'
    ? 'bg-red-600 hover:bg-red-700 text-white'
    : variant==='secondary'
    ? 'bg-gray-100 hover:bg-gray-200 text-gray-900'
    : 'bg-blue-600 hover:bg-blue-700 text-white'
  return <button onClick={onClick} className={`px-3 py-2 rounded-lg text-sm font-medium ${styles}`} {...props}>{children}</button>
}
function Input(props){ return <input {...props} className={`w-full px-3 py-2 border rounded-lg outline-none ${props.className||''}`} /> }
function Label({children}){ return <label className="block text-xs font-semibold text-gray-700 mb-1">{children}</label> }
function Card({children}){ return <div className="bg-white border rounded-2xl shadow-sm">{children}</div> }
function CardHeader({children}){ return <div className="px-4 py-3 border-b">{children}</div> }
function CardTitle({children}){ return <div className="font-semibold">{children}</div> }
function CardContent({children}){ return <div className="p-4">{children}</div> }

function NodeShell({ title, icon, accent }){
  return (
    <div className="rounded-2xl border bg-white min-w-[220px]">
      <div className="flex items-center gap-2 px-3 py-2 border-b" style={{borderColor: accent}}>
        <div className="p-1 rounded-lg" style={{ background: accent + '20' }}>{icon}</div>
        <span className="font-medium text-sm">{title}</span>
      </div>
    </div>
  )
}

const HttpActionNode = ({ data }) => (
  <div className="relative">
    <Handle type="target" position={Position.Left} />
    <NodeShell title={data?.name||'HTTP Action'} icon={<LinkIcon size={16}/>} accent="#0ea5e9" />
    <div className="px-3 py-2 text-xs text-gray-600">
      <span className="inline-block px-2 py-0.5 bg-blue-50 border border-blue-200 rounded mr-2">{data?.config?.method||'GET'}</span>
      <span className="truncate inline-block max-w-[180px] align-middle">{data?.config?.url||'https://'}</span>
    </div>
    <Handle type="source" position={Position.Right} />
  </div>
)

const DbActionNode = ({ data }) => (
  <div className="relative">
    <Handle type="target" position={Position.Left} />
    <NodeShell title={data?.name||'DB Action'} icon={<DatabaseIcon size={16}/>} accent="#22c55e" />
    <div className="px-3 py-2 text-xs text-gray-600 truncate max-w-[220px]">{data?.config?.engine||'postgresql'}</div>
    <Handle type="source" position={Position.Right} />
  </div>
)

const TransformActionNode = ({ data }) => (
  <div className="relative">
    <Handle type="target" position={Position.Left} />
    <NodeShell title={data?.name||'Transform'} icon={<BracesIcon size={16}/>} accent="#a855f7" />
    <div className="px-3 py-2 text-xs text-gray-600 truncate max-w-[220px]">{data?.config?.expression||'{out: input}'}</div>
    <Handle type="source" position={Position.Right} />
  </div>
)

const nodeTypes = { httpAction: HttpActionNode, dbAction: DbActionNode, transformAction: TransformActionNode }

function PaletteItem({ kind, label, icon }){
  const onDragStart = (e) => {
    e.dataTransfer.setData(DRAG_TYPE, kind)
    e.dataTransfer.effectAllowed = 'move'
  }
  return (
    <Button variant="secondary" draggable onDragStart={onDragStart} style={{display:'flex', alignItems:'center', gap:8, width:'100%'}}>
      {icon}{label}
    </Button>
  )
}

function PropertiesPanel({ selectedNode, onChange, onDelete }){
  if(!selectedNode){
    return (
      <Card className="h-full">
        <CardHeader><CardTitle>Properties</CardTitle></CardHeader>
        <CardContent className="text-sm text-gray-500">Select a node to edit its properties.</CardContent>
      </Card>
    )
  }
  const data = selectedNode.data
  return (
    <Card className="h-full">
      <CardHeader className="flex items-center justify-between">
        <div className="flex-1 min-w-0"><CardTitle>{data.name||'Node'}</CardTitle></div>
        <Button variant="danger" onClick={onDelete} title="Delete node"><Trash2 size={16}/></Button>
      </CardHeader>
      <CardContent>
        <div className="space-y-4" style={{maxHeight:'70vh', overflow:'auto', paddingRight:6}}>
          <div>
            <Label>Name</Label>
            <Input defaultValue={data.name} onChange={(e)=>onChange({ name: e.target.value })} />
          </div>

          {data.kind==='httpAction' && (<>
              <div>
                <Label>Method</Label>
                <select defaultValue={data.config.method} onChange={(e)=>onChange({ config:{...data.config, method: e.target.value } })} className="w-full px-3 py-2 border rounded-lg">
                  {['GET','POST','PUT','PATCH','DELETE'].map(m=> <option key={m} value={m}>{m}</option>)}
                </select>
              </div>
              <div>
                <Label>URL</Label>
                <Input defaultValue={data.config.url} placeholder="https://api.example.com" onChange={(e)=>onChange({ config:{...data.config, url: e.target.value } })} />
              </div>
              <div>
                <Label>Headers (JSON)</Label>
                <Input defaultValue={data.config.headers||'{}'} onChange={(e)=>onChange({ config:{...data.config, headers: e.target.value } })} />
              </div>
              <div>
                <Label>Query (JSON)</Label>
                <Input defaultValue={data.config.query||'{}'} onChange={(e)=>onChange({ config:{...data.config, query: e.target.value } })} />
              </div>
              <div>
                <Label>Body (JSON)</Label>
                <Input defaultValue={data.config.body||''} onChange={(e)=>onChange({ config:{...data.config, body: e.target.value } })} />
              </div>
              <div>
                <Label>Timeout (sec)</Label>
                <Input type="number" defaultValue={data.config.timeout||30} onChange={(e)=>onChange({ config:{...data.config, timeout: Number(e.target.value) } })} />
              </div>
            </>)}

          {data.kind==='dbAction' && (<>
              <div>
                <Label>Engine</Label>
                <select defaultValue={data.config.engine} onChange={(e)=>onChange({ config:{...data.config, engine: e.target.value } })} className="w-full px-3 py-2 border rounded-lg">
                  <option value="postgresql">PostgreSQL</option>
                  <option value="mysql">MySQL</option>
                  <option value="sqlite">SQLite</option>
                </select>
              </div>
              <div>
                <Label>Connection</Label>
                <Input defaultValue={data.config.connection} placeholder="postgresql+psycopg://user:pass@host:5432/db" onChange={(e)=>onChange({ config:{...data.config, connection: e.target.value } })} />
              </div>
              <div>
                <Label>SQL</Label>
                <Input defaultValue={data.config.sql} placeholder="INSERT INTO table(col) VALUES(:val)" onChange={(e)=>onChange({ config:{...data.config, sql: e.target.value } })} />
              </div>
              <div>
                <Label>Params (JSON)</Label>
                <Input defaultValue={data.config.params||'{}'} onChange={(e)=>onChange({ config:{...data.config, params: e.target.value } })} />
              </div>
            </>)}

          {data.kind==='transformAction' && (
            <div>
              <Label>Expression</Label>
              <Input defaultValue={data.config.expression} placeholder="({ out: input.value + 1 })" onChange={(e)=>onChange({ config:{...data.config, expression: e.target.value } })} />
            </div>
          )}
        </div>
      </CardContent>
    </Card>
  )
}

export default function App(){
  const [nodes, setNodes, onNodesChange] = useNodesState([])
  const [edges, setEdges, onEdgesChange] = useEdgesState([])
  const [selectedId, setSelectedId] = useState(null)
  const flowRef = useRef(null)

  const onConnect = useCallback((connection)=> setEdges((eds)=> addEdge({...connection}, eds)), [])
  const onDragOver = useCallback((e)=>{ e.preventDefault(); e.dataTransfer.dropEffect = 'move' }, [])
  const onDrop = useCallback((e)=>{
    e.preventDefault()
    const kind = e.dataTransfer.getData(DRAG_TYPE)
    if(!kind) return

    const bounds = flowRef.current?.getBoundingClientRect?.() ?? document.body.getBoundingClientRect()
    const pos = { x: e.clientX - bounds.left - 200, y: e.clientY - bounds.top - 40 }

    const id = `${kind}-${Date.now()}`
    let data
    if(kind==='httpAction') data = { kind:'httpAction', name:'HTTP Action', config:{ method:'GET', url:'https://api.example.com', headers:'{}', query:'{}', body:'', timeout:30 } }
    else if(kind==='dbAction') data = { kind:'dbAction', name:'DB Action', config:{ engine:'postgresql', connection:'', sql:'SELECT 1', params:'{}' } }
    else data = { kind:'transformAction', name:'Transform', config:{ expression:'({ out: input })' } }

    setNodes((nds)=> nds.concat({ id, type: kind, position: pos, data }))
    setSelectedId(id)
  }, [])

  const selectedNode = useMemo(()=> nodes.find(n=>n.id===selectedId) || null, [nodes, selectedId])

  const handleChange = useCallback((patch)=>{
    if(!selectedNode) return
    setNodes((nds)=> nds.map(n=> n.id===selectedNode.id ? { ...n, data: { ...n.data, ...patch, config: { ...n.data.config, ...(patch?.config||{}) } } } : n))
  }, [selectedNode])

  const handleDelete = useCallback(()=>{
    if(!selectedNode) return
    const id = selectedNode.id
    setNodes((nds)=> nds.filter(n=> n.id!==id))
    setEdges((eds)=> eds.filter(e=> e.source!==id && e.target!==id))
    setSelectedId(null)
  }, [selectedNode])

  return (
    <div className="w-screen h-screen grid" style={{gridTemplateColumns:'280px 1fr 340px', gap:12, padding:16, background:'#f7f7f7'}}>
      {/* LEFT: Palette */}
      <Card>
        <CardHeader><CardTitle>Actions</CardTitle></CardHeader>
        <CardContent>
          <div className="flex flex-col gap-2">
            <PaletteItem kind="httpAction" label="HTTP Action" icon={<LinkIcon size={16}/>} />
            <PaletteItem kind="dbAction" label="DB Action" icon={<DatabaseIcon size={16}/>} />
            <PaletteItem kind="transformAction" label="Transform" icon={<BracesIcon size={16}/>} />
            <div className="text-xs text-gray-500 mt-2">Drag any action onto the canvas.</div>
          </div>
        </CardContent>
      </Card>

      {/* CENTER: Canvas */}
      <div className="h-full rounded-2xl border bg-white overflow-hidden" ref={flowRef}>
        <ReactFlow
          nodes={nodes}
          edges={edges}
          onNodesChange={onNodesChange}
          onEdgesChange={onEdgesChange}
          onConnect={onConnect}
          nodeTypes={nodeTypes}
          onDrop={onDrop}
          onDragOver={onDragOver}
          fitView
          onNodeClick={(_, node)=> setSelectedId(node.id)}
          onPaneClick={()=> setSelectedId(null)}
        >
          <Background />
          <MiniMap pannable zoomable />
          <Controls />
          <Panel position="top-right">
            <Button onClick={()=> alert('Run flow – connect to backend API later')} style={{display:'flex', alignItems:'center', gap:6}}>
              <PlayIcon size={16}/> Run
            </Button>
          </Panel>
        </ReactFlow>
      </div>

      {/* RIGHT: Properties */}
      <PropertiesPanel selectedNode={selectedNode} onChange={handleChange} onDelete={handleDelete} />
    </div>
  )
}
