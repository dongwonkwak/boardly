import log from "loglevel";

if (import.meta.env.MODE === 'development') {
  log.setLevel('debug');
} else {
  log.setLevel('warn');
}

export default log;