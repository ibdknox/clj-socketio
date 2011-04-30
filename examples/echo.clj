(ns clj-socketio.examples.echo
  (:use clj-socketio.core))

(def server (create 8080))
(def clients (atom #{}))

(on-connected server (fn [client]
                       (swap! clients conj client)))

(on-disconnect server (fn [client]
                        (swap! clients disj client)))

(on-message server (fn [client msg]
                     (println msg)))

(start server)
