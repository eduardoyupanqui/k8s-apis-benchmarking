data "kubectl_path_documents" "postgres" {
  pattern = "../postgres/*.yaml"
}

resource "kubectl_manifest" "postgres" {
  for_each  = toset(data.kubectl_path_documents.postgres.documents)
  yaml_body = each.value

  depends_on = [kubectl_manifest.minio]
}