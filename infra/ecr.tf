resource "aws_ecr_repository" "maen030-ecr" {
  name = "maen030"

  image_scanning_configuration {
    scan_on_push = false
  }
}