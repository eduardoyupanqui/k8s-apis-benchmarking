package main

import (
	"context"
	"fmt"
	"log"
	"net/http"

	"github.com/aws/aws-sdk-go/aws"
	"github.com/aws/aws-sdk-go/aws/credentials"
	"github.com/aws/aws-sdk-go/aws/session"
	"github.com/gin-gonic/gin"
	"github.com/jackc/pgx/v5/pgxpool"
)

// handler
type handler struct {
	// S3 seesion, should be shared
	sess *session.Session

	// Postgres connection pool
	dbpool *pgxpool.Pool

	// App configuration object
	config *Config
}

func main() {
	// Load app config from yaml file.
	var c Config
	c.loadConfig("config.yaml")

	// Initialize Gin handler.
	h := handler{config: &c}
	h.s3Connect()
	h.dbConnect()

	r := gin.Default()

	// Define handler functions for each endpoint.
	r.GET("/api/devices", h.getDevices)
	r.GET("/api/images", h.getImage)
	r.GET("/health", h.getHealth)

	// Start the main Gin HTTP server.
	log.Printf("Starting App on port %d", c.AppPort)
	r.Run(fmt.Sprintf(":%d", c.AppPort))
}

// getDevices responds with the list of all connected devices as JSON.
func (h *handler) getDevices(c *gin.Context) {
	c.JSON(http.StatusOK, devices())
}

// getImage downloads image from S3
func (h *handler) getImage(c *gin.Context) {
	// Download the image from S3.
	_, err := download(h.sess, h.config.S3Config.Bucket, "thumbnail.png")
	if err != nil {
		log.Printf("download failed: %v", err)
		c.JSON(http.StatusInternalServerError, gin.H{"message": "internal error"})
		return
	}

	// Generate a new image.
	image := NewImage()
	// Save the image ID and the last modified date to the database.
	Save(image, "go_image", h.dbpool)

	c.JSON(http.StatusOK, gin.H{"message": "saved"})
}

// getHealth responds with a HTTP 200 or 5xx on error.
func (h *handler) getHealth(c *gin.Context) {
	c.JSON(200, gin.H{"status": "up"})
}

// s3Connect initializes the S3 session.
func (h *handler) s3Connect() {
	// Get credentials to authorize with AWS S3 API.
	crds := credentials.NewStaticCredentials(h.config.S3Config.User, h.config.S3Config.Secret, "")

	// Create S3 config.
	s3c := aws.Config{
		Region:           &h.config.S3Config.Region,
		Endpoint:         &h.config.S3Config.Endpoint,
		S3ForcePathStyle: &h.config.S3Config.PathStyle,
		Credentials:      crds,
	}

	// Establish a new session with the AWS S3 API.
	h.sess = session.Must(session.NewSessionWithOptions(session.Options{
		SharedConfigState: session.SharedConfigEnable,
		Config:            s3c,
	}))
}

// dbConnect creates a connection pool to connect to Postgres.
func (h *handler) dbConnect() {
	url := fmt.Sprintf("postgres://%s:%s@%s:5432/%s",
		h.config.DbConfig.User, h.config.DbConfig.Password, h.config.DbConfig.Host, h.config.DbConfig.Database)

	// Connect to the Postgres database.
	dbpool, err := pgxpool.New(context.Background(), url)
	if err != nil {
		log.Fatalf("Unable to create connection pool: %s", err)
	}
	// defer dbpool.Close()

	h.dbpool = dbpool
}
