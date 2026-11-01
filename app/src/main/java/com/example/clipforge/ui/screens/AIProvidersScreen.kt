package com.example.clipforge.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.Computer
import androidx.compose.material.icons.filled.NetworkCheck
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.clipforge.ai.provider.TestConnectionResult
import com.example.clipforge.data.model.AIProviderEntity
import com.example.clipforge.ui.theme.StudioBackground
import com.example.clipforge.ui.theme.StudioPrimary
import com.example.clipforge.ui.theme.StudioSecondary
import com.example.clipforge.ui.theme.StudioSurface
import com.example.clipforge.ui.theme.StudioSurfaceBorder
import com.example.clipforge.ui.theme.StudioSurfaceElevated
import com.example.clipforge.ui.theme.StudioTertiary
import com.example.clipforge.ui.viewmodel.ClipForgeViewModel
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AIProvidersScreen(
  viewModel: ClipForgeViewModel,
  onNavigateBack: () -> Unit
) {
  val providers by viewModel.aiProviders.collectAsState()
  var editingProvider by remember { mutableStateOf<AIProviderEntity?>(null) }
  var testResult by remember { mutableStateOf<TestConnectionResult?>(null) }
  var isTesting by remember { mutableStateOf(false) }

  Scaffold(
    topBar = {
      TopAppBar(
        title = {
          Text("Universal AI Providers & BYOK", fontWeight = FontWeight.Bold, color = Color.White)
        },
        navigationIcon = {
          IconButton(onClick = onNavigateBack) {
            Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
          }
        },
        actions = {
          IconButton(
            onClick = {
              editingProvider = AIProviderEntity(
                id = UUID.randomUUID().toString(),
                name = "New AI Provider",
                type = "OPENAI",
                baseUrl = "https://api.openai.com",
                apiKey = "",
                selectedModel = "gpt-4o-mini",
                isEnabled = true,
                isLocal = false,
                availableModels = "gpt-4o,gpt-4o-mini"
              )
            }
          ) {
            Icon(Icons.Default.Add, contentDescription = "Add Provider", tint = StudioSecondary)
          }
        },
        colors = TopAppBarDefaults.topAppBarColors(containerColor = StudioBackground)
      )
    },
    containerColor = StudioBackground
  ) { innerPadding ->
    LazyColumn(
      modifier = Modifier
        .fillMaxSize()
        .padding(innerPadding)
        .padding(horizontal = 16.dp),
      verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
      item {
        Card(
          colors = CardDefaults.cardColors(containerColor = StudioSurfaceElevated),
          shape = RoundedCornerShape(12.dp),
          modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, StudioSurfaceBorder, RoundedCornerShape(12.dp))
        ) {
          Column(modifier = Modifier.padding(14.dp)) {
            Text("Provider-Agnostic Architecture", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
            Text(
              "ClipForge AI can route tasks across Google Gemini, OpenAI, Claude, DeepSeek, or your own local Ollama / LM Studio inference server running on your computer.",
              color = Color.LightGray,
              fontSize = 12.sp,
              modifier = Modifier.padding(top = 4.dp),
              lineHeight = 16.sp
            )
          }
        }
      }

      items(providers) { prov ->
        Card(
          colors = CardDefaults.cardColors(containerColor = StudioSurface),
          shape = RoundedCornerShape(14.dp),
          modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, if (prov.isEnabled) StudioPrimary else StudioSurfaceBorder, RoundedCornerShape(14.dp))
        ) {
          Column(modifier = Modifier.padding(16.dp)) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                  modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(if (prov.isLocal) StudioTertiary.copy(alpha = 0.2f) else StudioPrimary.copy(alpha = 0.2f)),
                  contentAlignment = Alignment.Center
                ) {
                  Icon(
                    if (prov.isLocal) Icons.Default.Computer else Icons.Default.Cloud,
                    contentDescription = null,
                    tint = if (prov.isLocal) StudioTertiary else StudioPrimary,
                    modifier = Modifier.size(20.dp)
                  )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                  Text(prov.name, color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                  Text(
                    text = if (prov.isLocal) "Local Server (${prov.baseUrl})" else "Cloud API (${prov.selectedModel})",
                    color = Color.Gray,
                    fontSize = 11.sp
                  )
                }
              }

              Switch(
                checked = prov.isEnabled,
                onCheckedChange = { isChecked ->
                  viewModel.saveProvider(prov.copy(isEnabled = isChecked))
                },
                colors = SwitchDefaults.colors(
                  checkedThumbColor = StudioSecondary,
                  checkedTrackColor = StudioPrimary
                )
              )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Text(
                text = "Model: ${prov.selectedModel}",
                color = StudioSecondary,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold
              )

              Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(
                  onClick = {
                    isTesting = true
                    viewModel.testProvider(prov) { res ->
                      isTesting = false
                      testResult = res
                    }
                  },
                  shape = RoundedCornerShape(8.dp),
                  colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White)
                ) {
                  Icon(Icons.Default.NetworkCheck, contentDescription = null, modifier = Modifier.size(14.dp))
                  Spacer(modifier = Modifier.width(4.dp))
                  Text("Test Connection", fontSize = 11.sp)
                }

                Button(
                  onClick = { editingProvider = prov },
                  colors = ButtonDefaults.buttonColors(containerColor = StudioSurfaceElevated),
                  shape = RoundedCornerShape(8.dp)
                ) {
                  Text("Edit", color = Color.White, fontSize = 11.sp)
                }
              }
            }
          }
        }
      }

      item {
        Spacer(modifier = Modifier.height(60.dp))
      }
    }
  }

  // Edit Provider Dialog
  editingProvider?.let { prov ->
    var name by remember { mutableStateOf(prov.name) }
    var type by remember { mutableStateOf(prov.type) }
    var baseUrl by remember { mutableStateOf(prov.baseUrl) }
    var apiKey by remember { mutableStateOf(prov.apiKey) }
    var model by remember { mutableStateOf(prov.selectedModel) }
    var isKeyVisible by remember { mutableStateOf(false) }

    AlertDialog(
      onDismissRequest = { editingProvider = null },
      title = { Text("Configure AI Provider", color = Color.White, fontWeight = FontWeight.Bold) },
      text = {
        Column(modifier = Modifier.fillMaxWidth()) {
          OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("Provider Name") },
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White)
          )
          Spacer(modifier = Modifier.height(8.dp))

          // Type Chips
          Text("Provider Type", color = Color.LightGray, fontSize = 11.sp)
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
          ) {
            listOf("GEMINI", "OPENAI", "CLAUDE", "OLLAMA_LOCAL").forEach { t ->
              FilterChip(
                selected = type == t,
                onClick = {
                  type = t
                  if (t == "OLLAMA_LOCAL") {
                    baseUrl = "http://192.168.1.100:11434"
                    model = "llama3:latest"
                  } else if (t == "GEMINI") {
                    baseUrl = "https://generativelanguage.googleapis.com"
                    model = "gemini-3.8-flash"
                  }
                },
                label = { Text(t.split("_").first(), fontSize = 10.sp) },
                colors = FilterChipDefaults.filterChipColors(
                  selectedContainerColor = StudioPrimary,
                  selectedLabelColor = Color.White,
                  containerColor = StudioSurfaceElevated,
                  labelColor = Color.LightGray
                )
              )
            }
          }

          Spacer(modifier = Modifier.height(8.dp))
          OutlinedTextField(
            value = baseUrl,
            onValueChange = { baseUrl = it },
            label = { Text("Base URL / Host Address") },
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White)
          )

          Spacer(modifier = Modifier.height(8.dp))
          OutlinedTextField(
            value = apiKey,
            onValueChange = { apiKey = it },
            label = { Text("API Key (BYOK)") },
            visualTransformation = if (isKeyVisible) VisualTransformation.None else PasswordVisualTransformation(),
            trailingIcon = {
              IconButton(onClick = { isKeyVisible = !isKeyVisible }) {
                Icon(
                  if (isKeyVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                  contentDescription = null,
                  tint = Color.Gray
                )
              }
            },
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White)
          )

          Spacer(modifier = Modifier.height(8.dp))
          OutlinedTextField(
            value = model,
            onValueChange = { model = it },
            label = { Text("Model Identifier") },
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White)
          )
        }
      },
      confirmButton = {
        Button(
          onClick = {
            val updated = prov.copy(
              name = name,
              type = type,
              baseUrl = baseUrl,
              apiKey = apiKey,
              selectedModel = model,
              isLocal = type.contains("LOCAL")
            )
            viewModel.saveProvider(updated)
            editingProvider = null
          },
          colors = ButtonDefaults.buttonColors(containerColor = StudioPrimary)
        ) {
          Text("Save Provider")
        }
      },
      dismissButton = {
        TextButton(onClick = { editingProvider = null }) {
          Text("Cancel", color = Color.Gray)
        }
      },
      containerColor = Color(0xFF1E202B)
    )
  }

  // Test Connection Result Dialog
  testResult?.let { res ->
    AlertDialog(
      onDismissRequest = { testResult = null },
      title = {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Icon(
            if (res.success) Icons.Default.CheckCircle else Icons.Default.NetworkCheck,
            contentDescription = null,
            tint = if (res.success) Color(0xFF10B981) else Color(0xFFEF4444)
          )
          Spacer(modifier = Modifier.width(8.dp))
          Text(if (res.success) "Connection Succeeded!" else "Connection Failed", color = Color.White, fontWeight = FontWeight.Bold)
        }
      },
      text = {
        Column {
          Text(res.message, color = Color.LightGray, fontSize = 13.sp)
          if (res.latencyMs > 0) {
            Spacer(modifier = Modifier.height(6.dp))
            Text("Latency: ${res.latencyMs}ms", color = StudioSecondary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
          }
          if (res.discoveredModels.isNotEmpty()) {
            Spacer(modifier = Modifier.height(10.dp))
            Text("Discovered Models:", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            res.discoveredModels.take(4).forEach { m ->
              Text("• $m", color = Color.LightGray, fontSize = 11.sp)
            }
          }
        }
      },
      confirmButton = {
        TextButton(onClick = { testResult = null }) {
          Text("OK", color = StudioPrimary)
        }
      },
      containerColor = Color(0xFF1E202B)
    )
  }
}
