import http from 'k6/http';
import { sleep } from 'k6';

export const options = {
  // A number specifying the number of VUs to run concurrently.
  vus: 100,
  // A string specifying the total duration of the test run.
  duration: '200s',
};

// The function that defines VU logic.
export default async function() {
  http.get('http://192.168.56.246:8000/api/images');
  sleep(0.01);
}

