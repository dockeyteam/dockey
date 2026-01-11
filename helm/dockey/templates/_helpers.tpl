{{/*
Expand the name of the chart.
*/}}
{{- define "dockey.name" -}}
{{- default .Chart.Name .Values.nameOverride | trunc 63 | trimSuffix "-" }}
{{- end }}

{{/*
Create a default fully qualified app name.
*/}}
{{- define "dockey.fullname" -}}
{{- if .Values.nameOverride }}
{{- .Values.nameOverride | trunc 63 | trimSuffix "-" }}
{{- else }}
{{- printf "%s-%s" .Release.Name .Chart.Name | trunc 63 | trimSuffix "-" }}
{{- end }}
{{- end }}

{{/*
Create chart name and version as used by the chart label.
*/}}
{{- define "dockey.chart" -}}
{{- printf "%s-%s" .Chart.Name .Chart.Version | replace "+" "_" | trunc 63 | trimSuffix "-" }}
{{- end }}

{{/*
Common labels
*/}}
{{- define "dockey.labels" -}}
helm.sh/chart: {{ include "dockey.chart" . }}
{{ include "dockey.selectorLabels" . }}
{{- if .Chart.AppVersion }}
app.kubernetes.io/version: {{ .Chart.AppVersion | quote }}
{{- end }}
app.kubernetes.io/managed-by: {{ .Release.Service }}
{{- end }}

{{/*
Selector labels
*/}}
{{- define "dockey.selectorLabels" -}}
app.kubernetes.io/name: {{ include "dockey.name" . }}
app.kubernetes.io/instance: {{ .Release.Name }}
{{- end }}

{{/*
Create the name of the service account to use
*/}}
{{- define "dockey.serviceAccountName" -}}
{{- if .Values.serviceAccount.create }}
{{- default (include "dockey.fullname" .) .Values.serviceAccount.name }}
{{- else }}
{{- default "default" .Values.serviceAccount.name }}
{{- end }}
{{- end }}

{{/*
Namespace
*/}}
{{- define "dockey.namespace" -}}
{{- default .Values.global.namespace .Release.Namespace }}
{{- end }}

{{/*
Image registry
*/}}
{{- define "dockey.imageRegistry" -}}
{{- .Values.global.imageRegistry }}
{{- end }}

{{/*
Image pull policy
*/}}
{{- define "dockey.imagePullPolicy" -}}
{{- .Values.global.imagePullPolicy }}
{{- end }}

{{/*
Storage class
*/}}
{{- define "dockey.storageClass" -}}
{{- .Values.global.storageClass }}
{{- end }}
