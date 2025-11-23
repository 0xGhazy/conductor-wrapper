// import { AppBar, Toolbar, Button, Stack } from "@mui/material";
// import Sidebar from "./Components/Sidebar";
// import Canvas from "./Components/Canvas";
// import { useStudio } from "./Utils/store";
// import React, { useCallback, useMemo, useRef, useState, useEffect } from "react";


// const DRAWER_WIDTH = 360;

// export default function App() {
//   const { addStatus, removeSelected, exportBlueprint, importBlueprint } =
//     useStudio();

//   return (
//     <>
//       <AppBar position="static">
//         <Toolbar>
//           <Stack direction="row" spacing={1}>
//             <Button color="inherit" onClick={() => addStatus("New status")}>
//               Add Status
//             </Button>
//             <Button color="inherit" onClick={removeSelected}>
//               Delete Selected
//             </Button>
//             <Button
//               color="inherit"
//               onClick={() => {
//                 const json = JSON.stringify(exportBlueprint(), null, 2);
//                 const blob = new Blob([json], { type: "application/json" });
//                 const url = URL.createObjectURL(blob);
//                 const a = document.createElement("a");
//                 a.href = url;
//                 a.download = "blueprint.json";
//                 a.click();
//                 URL.revokeObjectURL(url);
//               }}
//             >
//               Export JSON
//             </Button>

//             <input
//               id="import-json"
//               type="file"
//               accept="application/json"
//               hidden
//               onChange={async (e) => {
//                 const f = e.target.files?.[0];
//                 if (!f) return;
//                 importBlueprint(JSON.parse(await f.text()));
//               }}
//             />
//             <Button
//               color="inherit"
//               onClick={() => document.getElementById("import-json")?.click()}
//             >
//               Import JSON
//             </Button>
//           </Stack>
//         </Toolbar>
//       </AppBar>

//       {/* Give the canvas space so it doesn't underlap the drawer */}
//       <div style={{ marginRight: DRAWER_WIDTH }}>
//         <Canvas />
//       </div>
//       <Sidebar />
//     </>
//   );
// }
