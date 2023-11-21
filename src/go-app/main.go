package main

import (
	"fmt"
	"log"
	"net/http"

	"github.com/gin-gonic/gin"
)

// handler
type handler struct {
	// App configuration object
	config *Config
}

func main() {
	// Load app config from yaml file.
	var c Config
	c.loadConfig("config.yaml")

	// Initialize Gin handler.
	h := handler{config: &c}

	r := gin.Default()

	// Define handler functions for each endpoint.
	r.GET("/api/devices", h.getDevices)
	r.GET("/health", h.getHealth)

	// Start the main Gin HTTP server.
	log.Printf("Starting App on port %d", c.AppPort)
	r.Run(fmt.Sprintf(":%d", c.AppPort))
}

// getDevices responds with the list of all connected devices as JSON.
func (h *handler) getDevices(c *gin.Context) {
	c.JSON(http.StatusOK, devices())
}

// getHealth responds with a HTTP 200 or 5xx on error.
func (h *handler) getHealth(c *gin.Context) {
	c.JSON(200, gin.H{"status": "up"})
}
