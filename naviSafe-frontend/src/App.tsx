import { BrowserRouter, Routes, Route } from "react-router-dom";
import { useFirebaseNotification } from './hooks/useNotification';
import { usePushMessage } from "./hooks/usePushMessage";
import {Home} from './screens/Home';
import { LocationPollingProvider } from './components/LocationProvider';
import { SrcAndDestination } from "./screens/SrcAndDestination";
import { Navigation3D } from "./screens/ Navigation3D";

function App() {
  useFirebaseNotification();
  usePushMessage();

  return (
    <BrowserRouter>
      <LocationPollingProvider />
      <Routes>
        <Route path = "/" element = { <Home />}/>
        <Route path = "/src-dest" element = { <SrcAndDestination />}/>
        <Route path = "/navigation" element = { <Navigation3D />}/>
      </Routes>
    </BrowserRouter>
    
  );
}

export default App
