import { Routes, Route } from "react-router-dom";
import DesignerPage from "./pages/DesignerPage";
import ConnectionPage from "./pages/connections/ConnectionsPage";

export default function App() {
  return (
      <Routes>
        <Route path="/designer" element={<DesignerPage />} />
        <Route path="/connections" element={<ConnectionPage />} />
      </Routes>
  );
}
