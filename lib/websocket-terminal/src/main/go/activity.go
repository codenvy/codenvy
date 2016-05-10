package main

import (
"time"
"log"
"net/http"
"os"
)

// time in seconds to wait, after last sent activity request, before next requests can be sent
const threshold int64 = 60

var apiEndpoint = os.Getenv("CHE_API_ENDPOINT")

var workspaceId = os.Getenv("CHE_WORKSPACE_ID")

type WorkspaceActivity struct {
	active bool
	lastUpdateTime int64
}

func (wa *WorkspaceActivity) Notify() {
	t := time.Now().Unix()
	if t < (wa.lastUpdateTime + threshold) {
		wa.active = true
	} else {
		go makeActivityRequest()
		wa.lastUpdateTime = t
	}
}

func (wa *WorkspaceActivity) StartTracking() {
	ticker := time.NewTicker(time.Minute)
	defer ticker.Stop()
	for _ = range ticker.C {
		if wa.active {
			go makeActivityRequest()
			wa.active = false
		}
	}
}

func makeActivityRequest() {
	req, _ := http.NewRequest(http.MethodPut, apiEndpoint + "/activity/" + workspaceId, nil)
	client := &http.Client{}
	_, err := client.Do(req)

	if err != nil {
		log.Printf("Failed to notify user activity in terminal: %s\n", err)
	}

}
