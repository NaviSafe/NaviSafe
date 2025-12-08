import { useFirebaseNotification } from './hooks/useNotification';
import {Home} from './screens/Home';
import { LocationPollingProvider } from './components/LocationProvider';

function App() {
  useFirebaseNotification();

  return (
    <>
      <LocationPollingProvider />
      <Home />
    </>
    
  );
}

export default App
