# P2P

Reupload of an old project from 2018.

Implementation of BitTorrent P2P protocol using multithreading where each peer functions as both a client and server. On account of multithreading, a number of thread-safe data structures like `ConcurrentHashMap` were used for concurrent retirevals and updates to various state variables.
