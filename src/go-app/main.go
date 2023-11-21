package main

import (
	"fmt"
	"log"

	"github.com/gin-gonic/gin"

)
// handler
type handler struct {
}

func main() {

	// Initialize Gin handler.
	h := handler{}

	r := gin.Default()

	// Define handler functions for each endpoint.
	r.GET("/health", h.getHealth)

	// Start the main Gin HTTP server.
	log.Printf("Starting App on port %d", 8000)
	r.Run(fmt.Sprintf(":%d", 8000))
}

// getHealth responds with a HTTP 200 or 5xx on error.
func (h *handler) getHealth(c *gin.Context) {
	c.JSON(200, gin.H{"status": "up"})
}