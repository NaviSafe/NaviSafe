import { useFirebaseNotification } from './hooks/useNotification';
import {Home} from './screens/Home';

function App() {
  useFirebaseNotification();

  return (
    <Home />
  );
}

export default App
