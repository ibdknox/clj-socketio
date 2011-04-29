(ns clj-socketio.examples.echo
  (:use clj-socketio.core))

(def clients (atom #{}))

(on-connected (fn [client]
                (swap! clients conj client)))

(on-disconnect (fn [client]
                   (swap! clients disj client)))

(on-message (fn [client msg]
              (println msg)))
