#!/bin/bash

PID_FILE=".pids"

if [ -f "$PID_FILE" ]; then
    echo "--- Stopping services using PIDs from $PID_FILE ---"
    
    # Read PIDs and kill them. The `-r` flag for xargs prevents it from running if the file is empty.
    xargs -r kill < "$PID_FILE"
    
    # Remove the PID file
    rm "$PID_FILE"
    
    echo "--- All services stopped. ---"
else
    echo "PID file ($PID_FILE) not found. Nothing to stop."
fi
