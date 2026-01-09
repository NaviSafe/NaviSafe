import { BrowserRouter, Routes, Route } from "react-router-dom";
import { useFirebaseNotification } from './hooks/useNotification';
import {Home} from './screens/Home';
import { LocationPollingProvider } from './components/LocationProvider';
import { SrcAndDestination } from "./screens/SrcAndDestination";

function App() {
  useFirebaseNotification();

  return (
    <BrowserRouter>
      <LocationPollingProvider />
      <Routes>
        <Route path = "/" element = { <Home />}/>
        <Route path = "/src-dest" element = { <SrcAndDestination />}/>
      </Routes>
    </BrowserRouter>
    
  );
}

export default App
