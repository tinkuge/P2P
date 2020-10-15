# P2P

Reupload of an old project from 2018. The project description can be found [here](https://github.com/tinkuge/P2P/blob/main/project_description.pdf)

Implementation of BitTorrent P2P protocol using multithreading where each peer functions as both a client and server. On account of multithreading, thread-safe data structures like `ConcurrentHashMap` and `synchronized` methods were employed for concurrent access to various state variables and methods.
