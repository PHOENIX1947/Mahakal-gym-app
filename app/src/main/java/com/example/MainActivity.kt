package com.example

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.foundation.gestures.scrollBy
import androidx.compose.ui.text.TextStyle
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.sin

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    setContent {
      MyApplicationTheme {
        MainScreen()
      }
    }
  }
}

// --- Navigation Screens enum ---
enum class Screen(val title: String, val icon: ImageVector, val tag: String) {
  Home("Home", Icons.Default.Home, "bnav_home"),
  Plans("Plans", Icons.Default.CreditCard, "bnav_plans"),
  Courses("Courses", Icons.Default.FitnessCenter, "bnav_courses"),
  Health("Health", Icons.Default.Favorite, "bnav_health"),
  More("More", Icons.Default.Menu, "bnav_more")
}

// --- Data Models ---
data class ParticleState(
  val xPct: Float,
  val size: Float,
  val speed: Float,
  val alpha: Float,
  val isGold: Boolean,
  val delayOffset: Float
)

data class GymPlan(
  val name: String,
  val tier: String,
  val price: String,
  val period: String,
  val isFeatured: Boolean,
  val isPremium: Boolean,
  val features: List<String>,
  val missingFeatures: List<String> = emptyList()
)

data class GymCourse(
  val name: String,
  val tag: String,
  val icon: ImageVector,
  val description: String,
  val startColor: Color,
  val endColor: Color
)

data class Trainer(
  val name: String,
  val specialty: String,
  val expYears: String,
  val rating: String,
  val avatarText: String,
  val startColor: Color,
  val endColor: Color
)

data class Review(
  val author: String,
  val text: String,
  val stars: Int,
  val avatar: String
)

// --- Sample Data Configurations ---
val plansList = listOf(
  GymPlan(
    name = "Premium PT",
    tier = "Personal Training",
    price = "4,999",
    period = "month",
    isFeatured = false,
    isPremium = true,
    features = listOf(
      "Unlimited gym access",
      "Full Guidance",
      "Full Nutrition Guide",
      "Locker Room Access",
      "Perfect Workout Guidance",
      "Customize Diet Plan",
      "Personal Training Sessions",
      "Customize Workout Routine"
    )
  ),
  GymPlan(
    name = "Personal Trainer",
    tier = "Personal Training",
    price = "1,999",
    period = "month",
    isFeatured = false,
    isPremium = true,
    features = listOf(
      "Unlimited gym access",
      "Full Guidance",
      "Full Nutrition Guide",
      "Perfect Workout Guidance",
      "Customize Diet Plan",
      "Personal Training Sessions",
      "Customize Workout Routine"
    )
  ),
  GymPlan(
    name = "Monthly",
    tier = "Starter",
    price = "699",
    period = "month",
    isFeatured = false,
    isPremium = false,
    features = listOf(
      "Unlimited gym access",
      "Cardio & machine zone",
      "Full facility access",
      "Custom meal plan",
      "2 classes/month on any 2 courses"
    ),
    missingFeatures = listOf("Personal trainer sessions")
  ),
  GymPlan(
    name = "3 Month",
    tier = "Save ₹198",
    price = "1,899",
    period = "3 mo",
    isFeatured = false,
    isPremium = false,
    features = listOf(
      "Unlimited gym access",
      "Cardio & machine zone",
      "Full facility access",
      "Custom meal plan",
      "3 classes/month on any 3 courses"
    ),
    missingFeatures = listOf("Personal trainer sessions")
  ),
  GymPlan(
    name = "6 Month",
    tier = "Save ₹595",
    price = "3,599",
    period = "6 mo",
    isFeatured = true,
    isPremium = false,
    features = listOf(
      "Unlimited gym access",
      "Cardio & machine zone",
      "Full facility access",
      "Custom meal plan",
      "6 classes/month on any 3 courses"
    ),
    missingFeatures = listOf("Personal trainer sessions")
  ),
  GymPlan(
    name = "Yearly",
    tier = "Save ₹2,889",
    price = "5,499",
    period = "year",
    isFeatured = true,
    isPremium = false,
    features = listOf(
      "Unlimited gym access",
      "Cardio & machine zone",
      "Full facility access",
      "Custom meal plan",
      "9 classes/month on any 3 courses"
    ),
    missingFeatures = listOf("Personal trainer sessions")
  )
)

val coursesList = listOf(
  GymCourse("Multi Gym", "Most Popular", Icons.Default.FitnessCenter, "State-of-the-art strength training zone with over 30+ custom machines.", GymRed, GymSurface2),
  GymCourse("Yoga", "New", Icons.Default.SelfImprovement, "Enhance flexibility, core strength, and breath control in our calming sessions.", GymGold, GymSurface2),
  GymCourse("Nunchaku", "New", Icons.Default.Security, "Ancient martial arts training focusing on hand-eye coordination and speed.", GymRed, GymSurface2),
  GymCourse("Martial Art", "New", Icons.Default.SportsMartialArts, "Professional discipline, self-defense moves, and physical empowerment.", GymGold, GymSurface2),
  GymCourse("Lathi", "New", Icons.Default.Hardware, "Traditional Indian stick martial arts, improving agility, blocks, and strikes.", GymRed, GymSurface2),
  GymCourse("Boxing", "New", Icons.Default.SportsKabaddi, "Cardio-intensive training focusing on speed, punching form, and core power.", GymGold, GymSurface2),
  GymCourse("Self Defense", "New", Icons.Default.Shield, "Crucial practical defense strategies for real-life safety situations.", GymRed, GymSurface2),
  GymCourse("Meditation", "New", Icons.Default.Spa, "Mindfulness relaxation, stress control, and deep breathing coaching.", GymGold, GymSurface2)
)

val trainersList = listOf(
  Trainer("Ranjit Dhara", "Gym Trainer", "15+", "4.9★", "RD", GymRed, GymSurface3),
  Trainer("Atanu Roy", "Gym Trainer", "4+", "4.8★", "AR", GymGold, GymSurface3),
  Trainer("Ankita Manna", "Yoga · Meditation", "12+", "4.9★", "AM", GymRed, GymSurface3)
)

val reviewsList = listOf(
  Review("Soumya Guchait", "Mahakal Gym is truly the best place for fitness lovers! The environment is very positive and motivating. Sir and Ma'am guide each member personally — workouts, diet, or overall lifestyle. If you're looking for a gym where you feel like part of a family, this is it. Highly recommended!", 5, "💪"),
  Review("Sisir Bag", "Having a gym like this in a rural area is very rare. The equipment is quite impressive. Must visit place.", 5, "🏃"),
  Review("Tamajit Bairi", "Mahakal Gym offers a top-notch fitness experience with modern equipment, expert trainers, and a clean, motivating environment. Personalized workout plans and affordable memberships make it a great choice.", 5, "🏋️"),
  Review("Rudranil Jana", "Having frequented this gymnasium for several months, I have observed remarkable transformations in both my physical and emotional well-being. The facility boasts superlative infrastructure, surpassing comparable local establishments. Highly recommended.", 5, "🏋️")
)

// --- Main App Screen ---
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen() {
  val context = LocalContext.current
  var currentScreen by rememberSaveable { mutableStateOf(Screen.Home) }
  var isLoading by remember { mutableStateOf(true) }
  var loadProgress by remember { mutableFloatStateOf(0f) }
  var loadText by remember { mutableStateOf("Initializing...") }

  // --- Loader Simulation ---
  LaunchedEffect(Unit) {
    val steps = listOf("Loading Assets...", "Setting Up Gym...", "Almost Ready...", "Let's Go 💪")
    for (i in 0..100 step 4) {
      loadProgress = i / 100f
      val stepIdx = ((loadProgress * steps.size).toInt()).coerceIn(0, steps.size - 1)
      loadText = steps[stepIdx]
      delay(80)
    }
    delay(200)
    isLoading = false
  }

  // --- Dynamic Particle State ---
  val particles = remember {
    List(40) {
      ParticleState(
        xPct = (0..100).random() / 100f,
        size = (2..6).random().toFloat(),
        speed = (5..15).random() / 10f,
        alpha = (1..6).random() / 10f,
        isGold = (0..10).random() > 7,
        delayOffset = (0..360).random().toFloat()
      )
    }
  }

  val infiniteTransition = rememberInfiniteTransition(label = "particles")
  val animState = infiniteTransition.animateFloat(
    initialValue = 0f,
    targetValue = 1000f,
    animationSpec = infiniteRepeatable(
      animation = tween(40000, easing = LinearEasing),
      repeatMode = RepeatMode.Restart
    ),
    label = "yTranslation"
  )

  Box(
    modifier = Modifier
      .fillMaxSize()
      .background(GymBackground)
      .drawBehind {
        // Render 3D-like red Grid Lines of the gym
        val gridSpacing = 50.dp.toPx()
        val lineAlpha = 0.03f
        val redColor = GymRed

        for (x in 0..size.width.toInt() step gridSpacing.toInt()) {
          drawLine(
            color = redColor,
            start = Offset(x.toFloat(), 0f),
            end = Offset(x.toFloat(), size.height),
            alpha = lineAlpha,
            strokeWidth = 1f
          )
        }
        for (y in 0..size.height.toInt() step gridSpacing.toInt()) {
          drawLine(
            color = redColor,
            start = Offset(0f, y.toFloat()),
            end = Offset(size.width, y.toFloat()),
            alpha = lineAlpha,
            strokeWidth = 1f
          )
        }

        // Render floating particles
        particles.forEach { p ->
          val yProgress = ((animState.value * p.speed) + p.delayOffset) % size.height
          val yPos = size.height - yProgress
          val xPos = p.xPct * size.width

          // Subtle bounce movement
          val xOffset = sin(yProgress * 0.02f) * 15f
          val finalColor = if (p.isGold) GymGold else GymRed
          val particleAlpha = p.alpha * (1f - (yProgress / size.height)) // Fade near top

          drawCircle(
            color = finalColor,
            radius = p.size,
            center = Offset(xPos + xOffset, yPos),
            alpha = particleAlpha.coerceIn(0f, 1f)
          )
        }
      }
  ) {
    if (isLoading) {
      // --- SPLASH SCREEN ---
      Column(
        modifier = Modifier
          .fillMaxSize()
          .background(GymBackground)
          .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
      ) {
        // Styled Gym Logo
        Box(
          modifier = Modifier
            .size(120.dp)
            .clip(CircleShape)
            .background(Brush.radialGradient(listOf(GymRed.copy(alpha = 0.25f), Color.Transparent)))
            .padding(8.dp),
          contentAlignment = Alignment.Center
        ) {
          Icon(
            imageVector = Icons.Default.FitnessCenter,
            contentDescription = "Logo icon",
            tint = GymRed,
            modifier = Modifier.size(64.dp)
          )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
          text = "MAHAKAL GYM",
          color = GymTextPrimary,
          fontSize = 32.sp,
          fontWeight = FontWeight.ExtraBold,
          fontFamily = FontFamily.SansSerif,
          letterSpacing = 2.sp
        )

        Text(
          text = "PREMIUM FITNESS STUDIO",
          color = GymRed,
          fontSize = 11.sp,
          fontWeight = FontWeight.Bold,
          letterSpacing = 4.sp,
          modifier = Modifier.alpha(0.7f)
        )

        Spacer(modifier = Modifier.height(40.dp))

        // Progress bar track
        Box(
          modifier = Modifier
            .width(180.dp)
            .height(2.dp)
            .background(GymSurface2)
        ) {
          Box(
            modifier = Modifier
              .fillMaxHeight()
              .fillMaxWidth(loadProgress)
              .background(Brush.horizontalGradient(listOf(GymRed, GymRedBright)))
              .shadow(8.dp, clip = false, ambientColor = GymRed, spotColor = GymRed)
          )
        }

        Spacer(modifier = Modifier.height(12.dp))

        Text(
          text = loadText,
          color = GymTextMuted,
          fontSize = 12.sp,
          fontWeight = FontWeight.SemiBold,
          letterSpacing = 1.sp
        )
      }
    } else {
      // --- APP SHELL ---
      Scaffold(
        containerColor = Color.Transparent,
        topBar = {
          TopAppBar(
            title = {
              Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                  Text(
                    text = "MAHAKAL",
                    color = GymTextPrimary,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 20.sp,
                    letterSpacing = 1.sp
                  )
                  Spacer(modifier = Modifier.width(4.dp))
                  Text(
                    text = "GYM",
                    color = GymRed,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 20.sp,
                    letterSpacing = 1.sp
                  )
                }
                Text(
                  text = "PREMIUM FITNESS STUDIO",
                  color = GymRed,
                  fontSize = 7.sp,
                  fontWeight = FontWeight.Bold,
                  letterSpacing = 2.sp,
                  modifier = Modifier.alpha(0.7f)
                )
              }
            },
            actions = {
              // Est. Badge
              Box(
                modifier = Modifier
                  .clip(RoundedCornerShape(3.dp))
                  .background(GymRed)
                  .padding(horizontal = 8.dp, vertical = 3.dp)
              ) {
                Text(
                  text = "EST. 2024",
                  color = Color.White,
                  fontSize = 8.sp,
                  fontWeight = FontWeight.Bold,
                  letterSpacing = 1.sp
                )
              }
              Spacer(modifier = Modifier.width(10.dp))
              // Quick Call icon
              IconButton(
                onClick = {
                  val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:+919609832596"))
                  context.startActivity(intent)
                },
                modifier = Modifier
                  .size(36.dp)
                  .clip(RoundedCornerShape(4.dp))
                  .border(1.dp, GymBorderBright, RoundedCornerShape(4.dp))
                  .background(GymSurface)
              ) {
                Icon(
                  imageVector = Icons.Default.Phone,
                  contentDescription = "Call Us",
                  tint = GymRed,
                  modifier = Modifier.size(16.dp)
                )
              }
            },
            colors = TopAppBarDefaults.topAppBarColors(
              containerColor = GymBackground.copy(alpha = 0.9f),
              titleContentColor = GymTextPrimary
            ),
            modifier = Modifier.drawBehind {
              // Bottom border red glow
              drawLine(
                brush = Brush.horizontalGradient(listOf(Color.Transparent, GymRed, Color.Transparent)),
                start = Offset(0f, size.height),
                end = Offset(size.width, size.height),
                strokeWidth = 2f
              )
            }
          )
        },
        bottomBar = {
          // Bottom Navigation Row
          NavigationBar(
            containerColor = GymBackground.copy(alpha = 0.96f),
            tonalElevation = 0.dp,
            modifier = Modifier
              .navigationBarsPadding()
              .drawBehind {
                drawLine(
                  brush = Brush.horizontalGradient(listOf(Color.Transparent, GymRed, Color.Transparent)),
                  start = Offset(0f, 0f),
                  end = Offset(size.width, 0f),
                  strokeWidth = 2f
                )
              }
          ) {
            Screen.values().forEach { screen ->
              val selected = currentScreen == screen
              NavigationBarItem(
                selected = selected,
                onClick = { currentScreen = screen },
                icon = {
                  Icon(
                    imageVector = screen.icon,
                    contentDescription = screen.title,
                    tint = if (selected) GymRed else GymTextMuted
                  )
                },
                label = {
                  Text(
                    text = screen.title,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (selected) GymRed else GymTextMuted
                  )
                },
                colors = NavigationBarItemDefaults.colors(
                  indicatorColor = GymRed.copy(alpha = 0.1f)
                ),
                modifier = Modifier.testTag(screen.tag)
              )
            }
          }
        },
        contentWindowInsets = WindowInsets.safeDrawing
      ) { innerPadding ->
        Box(
          modifier = Modifier
            .fillMaxSize()
            .padding(innerPadding)
        ) {
          AnimatedContent(
            targetState = currentScreen,
            transitionSpec = {
              fadeIn(animationSpec = tween(220)) + slideInVertically(
                animationSpec = tween(220),
                initialOffsetY = { 20 }
              ) togetherWith fadeOut(animationSpec = tween(150))
            },
            label = "screen_navigation"
          ) { screen ->
            when (screen) {
              Screen.Home -> PageHome(onNavigateTo = { currentScreen = it })
              Screen.Plans -> PagePlans()
              Screen.Courses -> PageCourses()
              Screen.Health -> PageHealth()
              Screen.More -> PageMore()
            }
          }
        }
      }
    }
  }
}

// ==========================================
// ═══ HOME SCREEN PAGE ═══
// ==========================================
@Composable
fun PageHome(onNavigateTo: (Screen) -> Unit) {
  val context = LocalContext.current
  val scrollState = rememberScrollState()

  // Auto-rotating slider index state for Gallery
  var galleryIndex by remember { mutableIntStateOf(0) }
  val totalGalleryItems = 6

  LaunchedEffect(Unit) {
    while (true) {
      delay(3800)
      galleryIndex = (galleryIndex + 1) % totalGalleryItems
    }
  }

  Column(
    modifier = Modifier
      .fillMaxSize()
      .verticalScroll(scrollState)
  ) {
    // --- Hero Header Card ---
    Box(
      modifier = Modifier
        .fillMaxWidth()
        .height(320.dp)
        .background(
          Brush.verticalGradient(
            listOf(GymBackground, GymSurface, GymBackground)
          )
        )
    ) {
      // Glow/Overlay Circle Behind Text
      Box(
        modifier = Modifier
          .align(Alignment.CenterEnd)
          .size(240.dp)
          .offset(x = 60.dp, y = (-20).dp)
          .clip(CircleShape)
          .background(Brush.radialGradient(listOf(GymRed.copy(alpha = 0.12f), Color.Transparent)))
      )

      Column(
        modifier = Modifier
          .fillMaxSize()
          .padding(20.dp),
        verticalArrangement = Arrangement.Bottom
      ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Box(
            modifier = Modifier
              .width(28.dp)
              .height(1.dp)
              .background(GymRed)
          )
          Spacer(modifier = Modifier.width(10.dp))
          Text(
            text = "Est. 2024  ·  Ghatal, WB",
            color = GymRed,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 2.sp
          )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
          text = "MAKE",
          color = GymTextPrimary,
          fontSize = 38.sp,
          fontWeight = FontWeight.ExtraBold,
          lineHeight = 40.sp
        )
        Text(
          text = "YOUR",
          color = GymRed,
          fontSize = 46.sp,
          fontWeight = FontWeight.ExtraBold,
          lineHeight = 46.sp,
          style = TextStyle(
            shadow = androidx.compose.ui.graphics.Shadow(
              color = GymRed.copy(alpha = 0.6f),
              offset = Offset(0f, 0f),
              blurRadius = 16f
            )
          )
        )
        Text(
          text = "LEGACY",
          color = GymTextPrimary,
          fontSize = 38.sp,
          fontWeight = FontWeight.ExtraBold,
          lineHeight = 40.sp
        )

        Spacer(modifier = Modifier.height(10.dp))

        Text(
          text = "Premium training, elite supplements & a community built on strength.",
          color = GymTextMuted,
          fontSize = 13.sp,
          fontWeight = FontWeight.Normal,
          maxLines = 2,
          overflow = TextOverflow.Ellipsis
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(
          onClick = {
            val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:+919609832596"))
            context.startActivity(intent)
          },
          colors = ButtonDefaults.buttonColors(containerColor = GymRed),
          shape = RoundedCornerShape(4.dp),
          contentPadding = PaddingValues(horizontal = 24.dp, vertical = 12.dp),
          modifier = Modifier.testTag("hero_join_button")
        ) {
          Text(
            text = "JOIN NOW",
            color = Color.White,
            fontWeight = FontWeight.ExtraBold,
            fontSize = 12.sp,
            letterSpacing = 1.sp
          )
        }
      }
    }

    // --- Dynamic Stats Grid Row ---
    // Animating the counters from 0 to target
    var runCounterAnimation by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
      delay(200)
      runCounterAnimation = true
    }

    val membersVal by animateIntAsState(
      targetValue = if (runCounterAnimation) 230 else 0,
      animationSpec = tween(1800, easing = FastOutSlowInEasing),
      label = "membersAnim"
    )
    val trainersVal by animateIntAsState(
      targetValue = if (runCounterAnimation) 3 else 0,
      animationSpec = tween(1800, easing = FastOutSlowInEasing),
      label = "trainersAnim"
    )
    val satisfiedVal by animateIntAsState(
      targetValue = if (runCounterAnimation) 97 else 0,
      animationSpec = tween(1800, easing = FastOutSlowInEasing),
      label = "satisfiedAnim"
    )
    val ratingVal by animateFloatAsState(
      targetValue = if (runCounterAnimation) 4.8f else 0.0f,
      animationSpec = tween(1800, easing = FastOutSlowInEasing),
      label = "ratingAnim"
    )

    Row(
      modifier = Modifier
        .fillMaxWidth()
        .background(GymSurface)
        .border(1.dp, GymBorder, RoundedCornerShape(0.dp))
        .drawBehind {
          // Inner grid borders
          val w = size.width / 4f
          for (i in 1..3) {
            drawLine(
              color = GymBorder,
              start = Offset(w * i, 0f),
              end = Offset(w * i, size.height),
              strokeWidth = 1f
            )
          }
        }
    ) {
      StatCell(numStr = "$membersVal+", label = "Members", modifier = Modifier.weight(1f))
      StatCell(numStr = "$trainersVal", label = "Trainers", modifier = Modifier.weight(1f))
      StatCell(numStr = "$satisfiedVal%", label = "Satisfied", modifier = Modifier.weight(1f))
      StatCell(numStr = String.format("%.1f★", ratingVal), label = "Rating", modifier = Modifier.weight(1f))
    }

    // --- Horizontal Scrolling Ticker ---
    HorizontalScrollingTicker()

    // --- Quick Access Grid Section ---
    Column(modifier = Modifier.padding(16.dp)) {
      Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
          modifier = Modifier
            .width(20.dp)
            .height(1.dp)
            .background(GymRed)
        )
        Spacer(modifier = Modifier.width(10.dp))
        Text(
          text = "Quick Access",
          color = GymRed,
          fontSize = 10.sp,
          fontWeight = FontWeight.Bold,
          letterSpacing = 2.sp
        )
      }
      Text(
        text = "EXPLORE GYM",
        color = GymTextPrimary,
        fontSize = 20.sp,
        fontWeight = FontWeight.ExtraBold,
        letterSpacing = 1.sp,
        modifier = Modifier.padding(top = 2.dp, bottom = 12.dp)
      )

      Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        QuickAccessBtn(iconStr = "💳", label = "Plans", modifier = Modifier.weight(1f)) { onNavigateTo(Screen.Plans) }
        QuickAccessBtn(iconStr = "🥊", label = "Courses", modifier = Modifier.weight(1f)) { onNavigateTo(Screen.Courses) }
        QuickAccessBtn(iconStr = "⚖️", label = "BMI / BMR", modifier = Modifier.weight(1f)) { onNavigateTo(Screen.Health) }
        QuickAccessBtn(iconStr = "📍", label = "Location", modifier = Modifier.weight(1f)) { onNavigateTo(Screen.More) }
      }
    }

    Spacer(modifier = Modifier.height(10.dp))
    Divider(color = GymBorder, thickness = 1.dp)

    // --- Custom Gym Zone Photo Slider ---
    Column(modifier = Modifier.padding(top = 20.dp, bottom = 12.dp)) {
      Text(
        text = "  Gallery",
        color = GymRed,
        fontSize = 10.sp,
        fontWeight = FontWeight.Bold,
        letterSpacing = 2.sp,
        modifier = Modifier.padding(horizontal = 16.dp)
      )
      Text(
        text = "  GYM PHOTOS",
        color = GymTextPrimary,
        fontSize = 20.sp,
        fontWeight = FontWeight.ExtraBold,
        letterSpacing = 1.sp,
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 2.dp)
      )

      Spacer(modifier = Modifier.height(10.dp))

      // Custom Simulated slider cards
      val zones = listOf(
        Triple("MULTI GYM AREA", "Heavy lifting & professional equipment.", Brush.linearGradient(listOf(GymRed, Color.Black))),
        Triple("CARDIO STUDIO", "Rowing, cycling, and intense running.", Brush.linearGradient(listOf(GymGold, Color.Black))),
        Triple("YOGA & CALISTHENICS ZONE", "Spacious floor for yoga & body stretching.", Brush.linearGradient(listOf(GymSurface3, GymSurface2))),
        Triple("MARTIAL ARTS RING", "Equipped for punching drills & sparring.", Brush.linearGradient(listOf(GymRed, GymSurface))),
        Triple("STRENGTH CORNER", "Rogue lifting plates & extreme kettlebells.", Brush.linearGradient(listOf(GymGold, GymSurface2))),
        Triple("LOCKER ROOMS", "Personal secure locker systems & showers.", Brush.linearGradient(listOf(GymSurface2, Color.Black)))
      )

      val activeZone = zones[galleryIndex]

      Box(
        modifier = Modifier
          .padding(horizontal = 16.dp)
          .fillMaxWidth()
          .height(180.dp)
          .clip(RoundedCornerShape(10.dp))
          .border(1.dp, GymBorder, RoundedCornerShape(10.dp))
          .background(activeZone.third)
      ) {
        // High-contrast background aesthetic lines
        Canvas(modifier = Modifier.fillMaxSize()) {
          drawLine(
            color = Color.White.copy(alpha = 0.04f),
            start = Offset(0f, 0f),
            end = Offset(size.width, size.height),
            strokeWidth = 4f
          )
          drawLine(
            color = Color.White.copy(alpha = 0.04f),
            start = Offset(size.width, 0f),
            end = Offset(0f, size.height),
            strokeWidth = 4f
          )
        }

        Column(
          modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
          verticalArrangement = Arrangement.SpaceBetween
        ) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Text(
              text = "ZONE ${galleryIndex + 1}",
              color = GymGold,
              fontSize = 11.sp,
              fontWeight = FontWeight.Bold,
              letterSpacing = 2.sp
            )
            Icon(
              imageVector = Icons.Default.Verified,
              contentDescription = "Verified Icon",
              tint = GymGold,
              modifier = Modifier.size(16.dp)
            )
          }

          Column {
            Text(
              text = activeZone.first,
              color = GymTextPrimary,
              fontSize = 22.sp,
              fontWeight = FontWeight.ExtraBold,
              fontFamily = FontFamily.SansSerif
            )
            Text(
              text = activeZone.second,
              color = GymTextMuted,
              fontSize = 12.sp,
              fontWeight = FontWeight.Normal
            )
          }
        }
      }

      // Slider Dots
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .padding(vertical = 12.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
      ) {
        for (i in 0 until totalGalleryItems) {
          val active = i == galleryIndex
          Box(
            modifier = Modifier
              .padding(horizontal = 3.dp)
              .size(if (active) 8.dp else 6.dp)
              .clip(CircleShape)
              .background(if (active) GymRed else GymTextMuted2)
              .clickable { galleryIndex = i }
          )
        }
      }
    }

    Divider(color = GymBorder, thickness = 1.dp)

    // --- Gym Features Section ---
    Column(modifier = Modifier.padding(top = 20.dp, bottom = 12.dp)) {
      Text(
        text = "  Why Choose Us",
        color = GymRed,
        fontSize = 10.sp,
        fontWeight = FontWeight.Bold,
        letterSpacing = 2.sp,
        modifier = Modifier.padding(horizontal = 16.dp)
      )
      Text(
        text = "  GYM FEATURES",
        color = GymTextPrimary,
        fontSize = 20.sp,
        fontWeight = FontWeight.ExtraBold,
        letterSpacing = 1.sp,
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 2.dp)
      )

      Spacer(modifier = Modifier.height(10.dp))

      FeatureItemRow(icon = "🏋️", title = "Elite Equipment", desc = "30+ machines — Technogym, Rogue & Life Fitness, maintained daily.")
      FeatureItemRow(icon = "🧠", title = "Expert Trainers", desc = "3 coaches: bodybuilding, yoga, karate & sport-specific performance.")
      FeatureItemRow(icon = "🌙", title = "Flexible Hours", desc = "Morning 5AM–1PM · Evening 3PM–9:30PM. Train on your schedule.")
      FeatureItemRow(icon = "❄️", title = "Fully Air Conditioned", desc = "Complete AC coverage — so your goals never stop.")
      FeatureItemRow(icon = "🏍", title = "3000 sq ft Parking", desc = "Dedicated lot — never think about parking again.")
      FeatureItemRow(icon = "💪", title = "1600 sq ft Gym Floor", desc = "Wide open training floor — room to lift, grow and move.")
      FeatureItemRow(icon = "🥤", title = "In-House Supplements", desc = "Curated from Optimum Nutrition, Whey Protein & exclusive blends.")
    }

    Spacer(modifier = Modifier.height(10.dp))
    Divider(color = GymBorder, thickness = 1.dp)

    // --- Reviews / Testimonials Section ---
    Column(modifier = Modifier.padding(vertical = 20.dp)) {
      Text(
        text = "  Member Stories",
        color = GymRed,
        fontSize = 10.sp,
        fontWeight = FontWeight.Bold,
        letterSpacing = 2.sp,
        modifier = Modifier.padding(horizontal = 16.dp)
      )
      Text(
        text = "  REVIEWS",
        color = GymTextPrimary,
        fontSize = 20.sp,
        fontWeight = FontWeight.ExtraBold,
        letterSpacing = 1.sp,
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 2.dp)
      )

      Spacer(modifier = Modifier.height(12.dp))

      LazyRow(
        modifier = Modifier.fillMaxWidth(),
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
      ) {
        items(reviewsList) { r ->
          ReviewCard(review = r)
        }
      }
    }

    // --- Bottom Footer Credit ---
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .background(GymSurface)
        .padding(24.dp),
      horizontalAlignment = Alignment.CenterHorizontally
    ) {
      Text(
        text = "© 2024 Mahakal Gym And Fitness Studio",
        color = GymTextMuted2,
        fontSize = 10.sp,
        fontWeight = FontWeight.Bold,
        letterSpacing = 1.sp
      )
      Text(
        text = "mahakal0808.github.io",
        color = GymTextMuted2,
        fontSize = 11.sp,
        fontWeight = FontWeight.Normal,
        modifier = Modifier.padding(top = 2.dp)
      )
    }
  }
}

@Composable
fun StatCell(numStr: String, label: String, modifier: Modifier = Modifier) {
  Column(
    modifier = modifier.padding(vertical = 16.dp),
    horizontalAlignment = Alignment.CenterHorizontally,
    verticalArrangement = Arrangement.Center
  ) {
    Text(
      text = numStr,
      color = GymRed,
      fontSize = 24.sp,
      fontWeight = FontWeight.ExtraBold,
      fontFamily = FontFamily.SansSerif
    )
    Spacer(modifier = Modifier.height(2.dp))
    Text(
      text = label.uppercase(),
      color = GymTextMuted,
      fontSize = 9.sp,
      fontWeight = FontWeight.ExtraBold,
      letterSpacing = 1.sp
    )
  }
}

@Composable
fun HorizontalScrollingTicker() {
  val items = listOf(
    "Elite Equipment", "Expert Coaching", "AC Gym", "Morning & Evening",
    "Premium Supplements", "Yoga & Meditation", "Martial Arts", "Personal Training"
  )

  val scrollState = rememberLazyListState()

  // Slow horizontal auto scroll loop
  LaunchedEffect(Unit) {
    while (true) {
      delay(30)
      try {
        scrollState.scrollBy(1.5f)
      } catch (e: Exception) {
        // Handle list offset limit, scroll back to 0
        scrollState.scrollToItem(0)
      }
    }
  }

  Box(
    modifier = Modifier
      .fillMaxWidth()
      .background(GymRed)
      .padding(vertical = 8.dp)
  ) {
    LazyRow(
      state = scrollState,
      userScrollEnabled = false,
      horizontalArrangement = Arrangement.spacedBy(24.dp),
      modifier = Modifier.fillMaxWidth()
    ) {
      // Repeat lists to simulate infinity scroll
      items(List(20) { items }.flatten()) { item ->
        Row(verticalAlignment = Alignment.CenterVertically) {
          Text(
            text = item.uppercase(),
            color = Color.White,
            fontSize = 11.sp,
            fontWeight = FontWeight.ExtraBold,
            letterSpacing = 1.sp
          )
          Spacer(modifier = Modifier.width(24.dp))
          Box(
            modifier = Modifier
              .size(4.dp)
              .clip(CircleShape)
              .background(Color.White.copy(alpha = 0.5f))
          )
        }
      }
    }
  }
}

@Composable
fun QuickAccessBtn(iconStr: String, label: String, modifier: Modifier = Modifier, onClick: () -> Unit) {
  Box(
    modifier = modifier
      .clip(RoundedCornerShape(8.dp))
      .border(1.dp, GymBorder, RoundedCornerShape(8.dp))
      .background(GymSurface2)
      .clickable(onClick = onClick)
      .padding(vertical = 14.dp, horizontal = 4.dp),
    contentAlignment = Alignment.Center
  ) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
      Text(text = iconStr, fontSize = 24.sp)
      Spacer(modifier = Modifier.height(6.dp))
      Text(
        text = label.uppercase(),
        color = GymTextMuted,
        fontSize = 10.sp,
        fontWeight = FontWeight.ExtraBold,
        letterSpacing = 1.sp,
        textAlign = TextAlign.Center
      )
    }
  }
}

@Composable
fun FeatureItemRow(icon: String, title: String, desc: String) {
  Row(
    modifier = Modifier
      .fillMaxWidth()
      .background(GymBackground)
      .padding(horizontal = 16.dp, vertical = 12.dp),
    verticalAlignment = Alignment.CenterVertically
  ) {
    Box(
      modifier = Modifier
        .size(40.dp)
        .clip(RoundedCornerShape(6.dp))
        .background(GymSurface2),
      contentAlignment = Alignment.Center
    ) {
      Text(text = icon, fontSize = 18.sp)
    }

    Spacer(modifier = Modifier.width(16.dp))

    Column(modifier = Modifier.weight(1.5f)) {
      Text(
        text = title.uppercase(),
        color = GymTextPrimary,
        fontSize = 13.sp,
        fontWeight = FontWeight.Bold,
        letterSpacing = 0.5.sp
      )
      Text(
        text = desc,
        color = GymTextMuted,
        fontSize = 11.sp,
        lineHeight = 15.sp,
        modifier = Modifier.padding(top = 1.dp)
      )
    }
  }
}

@Composable
fun ReviewCard(review: Review) {
  Box(
    modifier = Modifier
      .width(280.dp)
      .clip(RoundedCornerShape(10.dp))
      .border(1.dp, GymBorder, RoundedCornerShape(10.dp))
      .background(GymSurface)
      .padding(18.dp)
  ) {
    Column {
      Text(
        text = "\"",
        color = GymRed,
        fontSize = 32.sp,
        fontWeight = FontWeight.ExtraBold,
        lineHeight = 12.sp,
        modifier = Modifier.alpha(0.3f)
      )

      // Stars
      Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
        repeat(review.stars) {
          Icon(
            imageVector = Icons.Default.Star,
            contentDescription = "Star",
            tint = GymGold,
            modifier = Modifier.size(11.dp)
          )
        }
      }

      Spacer(modifier = Modifier.height(8.dp))

      Text(
        text = review.text,
        color = GymTextMuted,
        fontSize = 12.sp,
        lineHeight = 18.sp,
        maxLines = 5,
        overflow = TextOverflow.Ellipsis,
        modifier = Modifier.weight(1f, fill = false)
      )

      Spacer(modifier = Modifier.height(14.dp))
      Divider(color = GymBorder, thickness = 1.dp)
      Spacer(modifier = Modifier.height(10.dp))

      Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
          modifier = Modifier
            .size(32.dp)
            .clip(CircleShape)
            .background(GymSurface2)
            .border(1.dp, GymRed.copy(alpha = 0.2f), CircleShape),
          contentAlignment = Alignment.Center
        ) {
          Text(text = review.avatar, fontSize = 14.sp)
        }
        Spacer(modifier = Modifier.width(10.dp))
        Text(
          text = review.author,
          color = GymTextPrimary,
          fontSize = 12.sp,
          fontWeight = FontWeight.Bold,
          letterSpacing = 0.5.sp
        )
      }
    }
  }
}

// ==========================================
// ═══ PLANS SCREEN PAGE ═══
// ==========================================
@Composable
fun PagePlans() {
  val context = LocalContext.current
  val scrollState = rememberScrollState()

  Column(
    modifier = Modifier
      .fillMaxSize()
      .verticalScroll(scrollState)
  ) {
    // Top plans header
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .background(Brush.verticalGradient(listOf(GymRed.copy(alpha = 0.04f), Color.Transparent)))
        .padding(top = 20.dp, start = 16.dp, end = 16.dp, bottom = 10.dp)
    ) {
      Text(
        text = "Membership",
        color = GymRed,
        fontSize = 11.sp,
        fontWeight = FontWeight.Bold,
        letterSpacing = 2.sp
      )
      Text(
        text = "CHOOSE YOUR PLAN",
        color = GymTextPrimary,
        fontSize = 22.sp,
        fontWeight = FontWeight.ExtraBold,
        letterSpacing = 1.sp,
        modifier = Modifier.padding(top = 2.dp)
      )
    }

    // Membership registration fee card
    Box(
      modifier = Modifier
        .padding(horizontal = 16.dp, vertical = 6.dp)
        .fillMaxWidth()
        .clip(RoundedCornerShape(8.dp))
        .border(1.dp, GymRed.copy(alpha = 0.25f), RoundedCornerShape(8.dp))
        .background(GymSurface2)
        .padding(14.dp)
    ) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Column {
          Text(
            text = "One-time Registration Fee",
            color = GymTextPrimary,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.5.sp
          )
          Text(
            text = "Required for all standard plans",
            color = GymTextMuted,
            fontSize = 11.sp,
            modifier = Modifier.padding(top = 1.dp)
          )
        }
        Text(
          text = "₹1500",
          color = GymRed,
          fontSize = 20.sp,
          fontWeight = FontWeight.ExtraBold,
          fontFamily = FontFamily.SansSerif
        )
      }
    }

    Spacer(modifier = Modifier.height(10.dp))

    // Plans list
    Column(
      modifier = Modifier.padding(horizontal = 16.dp),
      verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
      plansList.forEach { plan ->
        PlanCard(plan = plan)
      }
    }

    Spacer(modifier = Modifier.height(20.dp))

    // Certification highlights
    Column(
      modifier = Modifier
        .padding(horizontal = 16.dp)
        .fillMaxWidth()
        .clip(RoundedCornerShape(10.dp))
        .border(1.dp, GymGold.copy(alpha = 0.25f), RoundedCornerShape(10.dp))
        .background(GymSurface2)
        .padding(16.dp)
    ) {
      Text(
        text = "Ranjit Dhara Certification",
        color = GymGold,
        fontSize = 13.sp,
        fontWeight = FontWeight.Bold,
        letterSpacing = 1.sp,
        modifier = Modifier.padding(bottom = 12.dp)
      )

      val highlights = listOf(
        "Unlimited Gym Access", "Perfect Workout Guidance",
        "Full Guidance", "Customize Diet Plan",
        "Full Nutrition Guide", "Personal Training",
        "Locker Room Access", "Custom Routine"
      )

      // 2-column grid inside cert
      Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        for (i in highlights.indices step 2) {
          Row(modifier = Modifier.fillMaxWidth()) {
            Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
              Text(text = "✦", color = GymGold, fontSize = 12.sp, modifier = Modifier.padding(end = 6.dp))
              Text(text = highlights[i], color = GymTextPrimary, fontSize = 11.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
            if (i + 1 < highlights.size) {
              Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
                Text(text = "✦", color = GymGold, fontSize = 12.sp, modifier = Modifier.padding(end = 6.dp))
                Text(text = highlights[i + 1], color = GymTextPrimary, fontSize = 11.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
              }
            }
          }
        }
      }
    }

    // Call to enroll section
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(24.dp),
      horizontalAlignment = Alignment.CenterHorizontally
    ) {
      Button(
        onClick = {
          val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:+919609832596"))
          context.startActivity(intent)
        },
        colors = ButtonDefaults.buttonColors(containerColor = GymRed),
        shape = RoundedCornerShape(6.dp),
        contentPadding = PaddingValues(horizontal = 24.dp, vertical = 14.dp),
        modifier = Modifier
          .fillMaxWidth()
          .shadow(8.dp, clip = false, ambientColor = GymRed, spotColor = GymRed)
          .testTag("plan_call_button")
      ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Icon(imageVector = Icons.Default.Phone, contentDescription = "Phone icon", modifier = Modifier.size(16.dp))
          Spacer(modifier = Modifier.width(8.dp))
          Text(
            text = "CALL TO ENROLL: +91 9609832596",
            color = Color.White,
            fontWeight = FontWeight.ExtraBold,
            fontSize = 11.sp,
            letterSpacing = 1.sp
          )
        }
      }
    }

    Spacer(modifier = Modifier.height(20.dp))
  }
}

@Composable
fun PlanCard(plan: GymPlan) {
  val borderStrokeColor = if (plan.isPremium) GymGold.copy(alpha = 0.35f) else if (plan.isFeatured) GymRed.copy(alpha = 0.35f) else GymBorder
  val cardBackground = GymSurface

  Box(
    modifier = Modifier
      .fillMaxWidth()
      .clip(RoundedCornerShape(10.dp))
      .border(1.dp, borderStrokeColor, RoundedCornerShape(10.dp))
      .background(cardBackground)
  ) {
    // Featured Badges on Card Top Right
    Row(
      modifier = Modifier
        .align(Alignment.TopEnd)
        .padding(12.dp)
    ) {
      if (plan.isPremium) {
        Box(
          modifier = Modifier
            .clip(RoundedCornerShape(3.dp))
            .background(GymGold)
            .padding(horizontal = 8.dp, vertical = 3.dp)
        ) {
          Text(
            text = "VIP",
            color = GymBackground,
            fontSize = 8.sp,
            fontWeight = FontWeight.ExtraBold,
            letterSpacing = 1.sp
          )
        }
      } else if (plan.isFeatured) {
        Box(
          modifier = Modifier
            .clip(RoundedCornerShape(3.dp))
            .background(GymRed)
            .padding(horizontal = 8.dp, vertical = 3.dp)
        ) {
          Text(
            text = "POPULAR",
            color = Color.White,
            fontSize = 8.sp,
            fontWeight = FontWeight.ExtraBold,
            letterSpacing = 1.sp
          )
        }
      }
    }

    Column(modifier = Modifier.padding(16.dp)) {
      Text(
        text = plan.tier.uppercase(),
        color = GymTextMuted,
        fontSize = 9.sp,
        fontWeight = FontWeight.ExtraBold,
        letterSpacing = 1.sp
      )
      Text(
        text = plan.name,
        color = if (plan.isPremium) GymGold else GymTextPrimary,
        fontSize = 20.sp,
        fontWeight = FontWeight.ExtraBold,
        letterSpacing = 0.5.sp
      )

      Row(
        verticalAlignment = Alignment.Bottom,
        modifier = Modifier.padding(vertical = 8.dp)
      ) {
        Text(
          text = "₹",
          color = if (plan.isPremium) GymGold else GymRed,
          fontSize = 18.sp,
          fontWeight = FontWeight.Bold,
          modifier = Modifier.padding(bottom = 3.dp)
        )
        Text(
          text = plan.price,
          color = if (plan.isPremium) GymGold else GymTextPrimary,
          fontSize = 28.sp,
          fontWeight = FontWeight.ExtraBold,
          fontFamily = FontFamily.SansSerif
        )
        Text(
          text = " / ${plan.period}",
          color = GymTextMuted,
          fontSize = 11.sp,
          fontWeight = FontWeight.SemiBold,
          modifier = Modifier.padding(bottom = 3.dp)
        )
      }

      Divider(color = GymBorder, thickness = 1.dp, modifier = Modifier.padding(vertical = 8.dp))

      // List of Features
      Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        plan.features.forEach { feature ->
          Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
              text = "✦",
              color = if (plan.isPremium) GymGold else GymRed,
              fontSize = 11.sp,
              modifier = Modifier.padding(end = 8.dp)
            )
            Text(
              text = feature,
              color = GymTextPrimary,
              fontSize = 12.sp,
              fontWeight = FontWeight.Medium
            )
          }
        }
        plan.missingFeatures.forEach { feature ->
          Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
              text = "✕",
              color = GymTextMuted2,
              fontSize = 11.sp,
              modifier = Modifier.padding(end = 8.dp)
            )
            Text(
              text = feature,
              color = GymTextMuted2,
              fontSize = 12.sp,
              fontWeight = FontWeight.Normal
            )
          }
        }
      }
    }
  }
}

// ==========================================
// ═══ COURSES SCREEN PAGE ═══
// ==========================================
@Composable
fun PageCourses() {
  val scrollState = rememberScrollState()

  Column(
    modifier = Modifier
      .fillMaxSize()
      .verticalScroll(scrollState)
  ) {
    // Courses Page Header
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .background(Brush.verticalGradient(listOf(GymRed.copy(alpha = 0.04f), Color.Transparent)))
        .padding(top = 20.dp, start = 16.dp, end = 16.dp, bottom = 12.dp)
    ) {
      Text(
        text = "All Courses",
        color = GymRed,
        fontSize = 11.sp,
        fontWeight = FontWeight.Bold,
        letterSpacing = 2.sp
      )
      Text(
        text = "OUR CLASSES",
        color = GymTextPrimary,
        fontSize = 22.sp,
        fontWeight = FontWeight.ExtraBold,
        letterSpacing = 1.sp,
        modifier = Modifier.padding(top = 2.dp)
      )
    }

    // Grid of courses
    Column(
      modifier = Modifier.padding(horizontal = 16.dp),
      verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
      for (i in coursesList.indices step 2) {
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
          CourseCard(course = coursesList[i], modifier = Modifier.weight(1f))
          if (i + 1 < coursesList.size) {
            CourseCard(course = coursesList[i + 1], modifier = Modifier.weight(1f))
          } else {
            Spacer(modifier = Modifier.weight(1f))
          }
        }
      }
    }

    Spacer(modifier = Modifier.height(24.dp))
    Divider(color = GymBorder, thickness = 1.dp)

    // Trainers Page Header
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(top = 20.dp, start = 16.dp, end = 16.dp, bottom = 12.dp)
    ) {
      Text(
        text = "Meet the Team",
        color = GymRed,
        fontSize = 11.sp,
        fontWeight = FontWeight.Bold,
        letterSpacing = 2.sp
      )
      Text(
        text = "OUR TRAINERS",
        color = GymTextPrimary,
        fontSize = 20.sp,
        fontWeight = FontWeight.ExtraBold,
        letterSpacing = 1.sp,
        modifier = Modifier.padding(top = 2.dp)
      )
    }

    // Grid of Trainers
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 16.dp),
      horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
      trainersList.forEach { trainer ->
        TrainerCard(trainer = trainer, modifier = Modifier.weight(1f))
      }
    }

    Spacer(modifier = Modifier.height(30.dp))
  }
}

@Composable
fun CourseCard(course: GymCourse, modifier: Modifier = Modifier) {
  Box(
    modifier = modifier
      .clip(RoundedCornerShape(10.dp))
      .border(1.dp, GymBorder, RoundedCornerShape(10.dp))
      .background(GymSurface)
  ) {
    Column {
      // Styled Image Box Fallback
      Box(
        modifier = Modifier
          .fillMaxWidth()
          .height(110.dp)
          .background(Brush.verticalGradient(listOf(course.startColor.copy(alpha = 0.2f), course.endColor)))
          .drawBehind {
            // Cool diagonal grid lines for high-tech style
            drawLine(
              color = course.startColor.copy(alpha = 0.08f),
              start = Offset(0f, 0f),
              end = Offset(size.width, size.height),
              strokeWidth = 3f
            )
          },
        contentAlignment = Alignment.Center
      ) {
        // Class Tag
        Box(
          modifier = Modifier
            .align(Alignment.TopStart)
            .padding(8.dp)
            .clip(RoundedCornerShape(3.dp))
            .background(GymRed)
            .padding(horizontal = 6.dp, vertical = 2.dp)
        ) {
          Text(
            text = course.tag.uppercase(),
            color = Color.White,
            fontSize = 7.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.5.sp
          )
        }

        // Custom Symbol representing class
        Icon(
          imageVector = course.icon,
          contentDescription = course.name,
          tint = course.startColor,
          modifier = Modifier.size(36.dp)
        )
      }

      Column(modifier = Modifier.padding(12.dp)) {
        Text(
          text = course.name.uppercase(),
          color = GymTextPrimary,
          fontSize = 13.sp,
          fontWeight = FontWeight.ExtraBold,
          letterSpacing = 0.5.sp,
          maxLines = 1,
          overflow = TextOverflow.Ellipsis
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
          text = course.description,
          color = GymTextMuted,
          fontSize = 11.sp,
          lineHeight = 14.sp,
          maxLines = 2,
          overflow = TextOverflow.Ellipsis
        )
      }
    }
  }
}

@Composable
fun TrainerCard(trainer: Trainer, modifier: Modifier = Modifier) {
  Box(
    modifier = modifier
      .clip(RoundedCornerShape(10.dp))
      .border(1.dp, GymBorder, RoundedCornerShape(10.dp))
      .background(GymSurface)
  ) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
      // Avatar placeholder box
      Box(
        modifier = Modifier
          .fillMaxWidth()
          .aspectRatio(0.9f)
          .background(Brush.linearGradient(listOf(trainer.startColor.copy(alpha = 0.15f), trainer.endColor))),
        contentAlignment = Alignment.Center
      ) {
        Box(
          modifier = Modifier
            .size(48.dp)
            .clip(CircleShape)
            .background(GymSurface2)
            .border(1.dp, trainer.startColor.copy(alpha = 0.4f), CircleShape),
          contentAlignment = Alignment.Center
        ) {
          Text(
            text = trainer.avatarText,
            color = trainer.startColor,
            fontSize = 16.sp,
            fontWeight = FontWeight.ExtraBold
          )
        }
      }

      Text(
        text = trainer.name,
        color = GymTextPrimary,
        fontSize = 11.sp,
        fontWeight = FontWeight.Bold,
        textAlign = TextAlign.Center,
        modifier = Modifier.padding(top = 8.dp, start = 4.dp, end = 4.dp),
        maxLines = 1,
        overflow = TextOverflow.Ellipsis
      )

      Text(
        text = trainer.specialty,
        color = GymRed,
        fontSize = 8.sp,
        fontWeight = FontWeight.SemiBold,
        textAlign = TextAlign.Center,
        modifier = Modifier.padding(bottom = 8.dp, start = 4.dp, end = 4.dp),
        maxLines = 1,
        overflow = TextOverflow.Ellipsis
      )

      // TSA metrics
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .border(1.dp, GymBorder, RoundedCornerShape(0.dp))
      ) {
        Column(
          modifier = Modifier
            .weight(1f)
            .padding(vertical = 6.dp),
          horizontalAlignment = Alignment.CenterHorizontally
        ) {
          Text(text = trainer.expYears, color = GymRed, fontSize = 11.sp, fontWeight = FontWeight.Bold)
          Text(text = "YRS", color = GymTextMuted2, fontSize = 7.sp, fontWeight = FontWeight.Bold)
        }
        Divider(modifier = Modifier
          .fillMaxHeight()
          .width(1.dp), color = GymBorder)
        Column(
          modifier = Modifier
            .weight(1f)
            .padding(vertical = 6.dp),
          horizontalAlignment = Alignment.CenterHorizontally
        ) {
          Text(text = trainer.rating, color = GymGold, fontSize = 11.sp, fontWeight = FontWeight.Bold)
          Text(text = "RATING", color = GymTextMuted2, fontSize = 7.sp, fontWeight = FontWeight.Bold)
        }
      }
    }
  }
}

// ==========================================
// ═══ HEALTH CALCULATORS PAGE ═══
// ==========================================
@Composable
fun PageHealth() {
  var activeTab by rememberSaveable { mutableStateOf("bmi") }

  Column(modifier = Modifier.fillMaxSize()) {
    // Health Switch Tab Row
    TabRow(
      selectedTabIndex = if (activeTab == "bmi") 0 else 1,
      containerColor = GymSurface,
      contentColor = GymRed,
      indicator = { tabPositions ->
        TabRowDefaults.SecondaryIndicator(
          modifier = Modifier.tabIndicatorOffset(tabPositions[if (activeTab == "bmi") 0 else 1]),
          color = GymRed
        )
      }
    ) {
      Tab(
        selected = activeTab == "bmi",
        onClick = { activeTab = "bmi" },
        text = {
          Text(
            text = "BMI CALCULATOR",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp
          )
        }
      )
      Tab(
        selected = activeTab == "bmr",
        onClick = { activeTab = "bmr" },
        text = {
          Text(
            text = "BMR CALCULATOR",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp
          )
        }
      )
    }

    Box(modifier = Modifier.fillMaxSize()) {
      if (activeTab == "bmi") {
        BMICalculatorView()
      } else {
        BMRCalculatorView()
      }
    }
  }
}

@Composable
fun BMICalculatorView() {
  val context = LocalContext.current
  val scrollState = rememberScrollState()

  var gender by rememberSaveable { mutableStateOf("male") }
  var unitSystem by rememberSaveable { mutableStateOf("metric") }
  var weightInput by rememberSaveable { mutableStateOf("") }
  var heightInput by rememberSaveable { mutableStateOf("") }
  var ageInput by rememberSaveable { mutableStateOf("") }

  var bmiResult by remember { mutableDoubleStateOf(0.0) }
  var calculatedStatus by remember { mutableStateOf("") }
  var calculatedColor by remember { mutableStateOf(Color.Green) }
  var calculatedTip by remember { mutableStateOf("") }
  var showResult by rememberSaveable { mutableStateOf(false) }

  Column(
    modifier = Modifier
      .fillMaxSize()
      .verticalScroll(scrollState)
      .padding(16.dp)
  ) {
    Box(
      modifier = Modifier
        .fillMaxWidth()
        .clip(RoundedCornerShape(10.dp))
        .border(1.dp, GymBorder, RoundedCornerShape(10.dp))
        .background(GymSurface2)
        .padding(16.dp)
    ) {
      Column {
        Text(
          text = "BMI CALCULATOR",
          color = GymTextPrimary,
          fontSize = 16.sp,
          fontWeight = FontWeight.ExtraBold,
          letterSpacing = 0.5.sp,
          modifier = Modifier.padding(bottom = 12.dp)
        )

        // Gender Choice Buttons
        Text(text = "GENDER", color = GymTextMuted, fontSize = 9.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
            .clip(RoundedCornerShape(4.dp))
            .border(1.dp, GymBorderBright, RoundedCornerShape(4.dp))
        ) {
          Button(
            onClick = { gender = "male" },
            colors = ButtonDefaults.buttonColors(
              containerColor = if (gender == "male") GymRed else Color.Transparent
            ),
            shape = RoundedCornerShape(0.dp),
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(0.dp)
          ) {
            Text(text = "♂ MALE", color = if (gender == "male") Color.White else GymTextMuted, fontSize = 11.sp, fontWeight = FontWeight.Bold)
          }
          Button(
            onClick = { gender = "female" },
            colors = ButtonDefaults.buttonColors(
              containerColor = if (gender == "female") GymRed else Color.Transparent
            ),
            shape = RoundedCornerShape(0.dp),
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(0.dp)
          ) {
            Text(text = "♀ FEMALE", color = if (gender == "female") Color.White else GymTextMuted, fontSize = 11.sp, fontWeight = FontWeight.Bold)
          }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Unit Choice Buttons
        Text(text = "UNIT SYSTEM", color = GymTextMuted, fontSize = 9.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
            .clip(RoundedCornerShape(4.dp))
            .border(1.dp, GymBorderBright, RoundedCornerShape(4.dp))
        ) {
          Button(
            onClick = { unitSystem = "metric" },
            colors = ButtonDefaults.buttonColors(
              containerColor = if (unitSystem == "metric") GymRed else Color.Transparent
            ),
            shape = RoundedCornerShape(0.dp),
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(0.dp)
          ) {
            Text(text = "METRIC (KG/CM)", color = if (unitSystem == "metric") Color.White else GymTextMuted, fontSize = 11.sp, fontWeight = FontWeight.Bold)
          }
          Button(
            onClick = { unitSystem = "imperial" },
            colors = ButtonDefaults.buttonColors(
              containerColor = if (unitSystem == "imperial") GymRed else Color.Transparent
            ),
            shape = RoundedCornerShape(0.dp),
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(0.dp)
          ) {
            Text(text = "IMPERIAL (LB/IN)", color = if (unitSystem == "imperial") Color.White else GymTextMuted, fontSize = 11.sp, fontWeight = FontWeight.Bold)
          }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Weight and Height inputs
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
          Column(modifier = Modifier.weight(1f)) {
            Text(
              text = if (unitSystem == "metric") "WEIGHT (KG)" else "WEIGHT (LB)",
              color = GymTextMuted,
              fontSize = 9.sp,
              fontWeight = FontWeight.Bold,
              letterSpacing = 1.sp
            )
            OutlinedTextField(
              value = weightInput,
              onValueChange = { weightInput = it },
              placeholder = { Text(text = if (unitSystem == "metric") "e.g. 75" else "e.g. 165", fontSize = 13.sp) },
              keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
              colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = GymRed,
                unfocusedBorderColor = GymBorderBright,
                focusedTextColor = GymTextPrimary,
                unfocusedTextColor = GymTextPrimary,
                focusedPlaceholderColor = GymTextMuted2,
                unfocusedPlaceholderColor = GymTextMuted2
              ),
              singleLine = true,
              modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp)
                .testTag("bmi_weight_input")
            )
          }

          Column(modifier = Modifier.weight(1f)) {
            Text(
              text = if (unitSystem == "metric") "HEIGHT (CM)" else "HEIGHT (IN)",
              color = GymTextMuted,
              fontSize = 9.sp,
              fontWeight = FontWeight.Bold,
              letterSpacing = 1.sp
            )
            OutlinedTextField(
              value = heightInput,
              onValueChange = { heightInput = it },
              placeholder = { Text(text = if (unitSystem == "metric") "e.g. 175" else "e.g. 69", fontSize = 13.sp) },
              keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
              colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = GymRed,
                unfocusedBorderColor = GymBorderBright,
                focusedTextColor = GymTextPrimary,
                unfocusedTextColor = GymTextPrimary,
                focusedPlaceholderColor = GymTextMuted2,
                unfocusedPlaceholderColor = GymTextMuted2
              ),
              singleLine = true,
              modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp)
                .testTag("bmi_height_input")
            )
          }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(text = "AGE (OPTIONAL)", color = GymTextMuted, fontSize = 9.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
        OutlinedTextField(
          value = ageInput,
          onValueChange = { ageInput = it },
          placeholder = { Text(text = "e.g. 28", fontSize = 13.sp) },
          keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
          colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = GymRed,
            unfocusedBorderColor = GymBorderBright,
            focusedTextColor = GymTextPrimary,
            unfocusedTextColor = GymTextPrimary,
            focusedPlaceholderColor = GymTextMuted2,
            unfocusedPlaceholderColor = GymTextMuted2
          ),
          singleLine = true,
          modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(
          onClick = {
            val w = weightInput.toDoubleOrNull()
            val h = heightInput.toDoubleOrNull()
            if (w == null || h == null || w <= 0.0 || h <= 0.0) {
              Toast.makeText(context, "Please enter valid values!", Toast.LENGTH_SHORT).show()
              return@Button
            }

            val computedBmi = if (unitSystem == "imperial") {
              (703.0 * w) / (h * h)
            } else {
              w / ((h / 100.0) * (h / 100.0))
            }

            bmiResult = Math.round(computedBmi * 10.0) / 10.0

            val rangesUnder = 18.5
            val rangesOver = if (gender == "female") 24.0 else 25.0
            val rangesObese = if (gender == "female") 29.0 else 30.0

            if (bmiResult < rangesUnder) {
              calculatedStatus = "Underweight"
              calculatedColor = Color(0xFF60A5FA) // Blue
              calculatedTip = "Focus on a caloric surplus with high-protein foods like chicken, eggs, fish, and whey protein to build healthy lean muscle mass safely."
            } else if (bmiResult < rangesOver) {
              calculatedStatus = "Healthy"
              calculatedColor = Color(0xFF4ADE80) // Green
              calculatedTip = "Great shape! Keep up the excellent work. Maintain your fitness with 3–5 intense gym sessions per week and standard healthy balanced nutrition."
            } else if (bmiResult < rangesObese) {
              calculatedStatus = "Overweight"
              calculatedColor = Color(0xFFFACC15) // Yellow
              calculatedTip = "A calorie deficit of 400–500 kcal/day combined with heavy strength resistance training at Mahakal Gym is recommended to shed fat and retain muscle."
            } else {
              calculatedStatus = "Obese"
              calculatedColor = Color(0xFFEF4444) // Red
              calculatedTip = "We can help! Our experienced personal trainers specialize in safe, friendly, judgment-free personal routines to build healthy lifestyle habits step-by-step."
            }

            showResult = true
          },
          colors = ButtonDefaults.buttonColors(containerColor = GymRed),
          shape = RoundedCornerShape(4.dp),
          modifier = Modifier
            .fillMaxWidth()
            .testTag("bmi_calculate_button")
        ) {
          Text(text = "CALCULATE BMI", fontWeight = FontWeight.ExtraBold, color = Color.White, letterSpacing = 1.sp)
        }

        // Animated Result Card
        if (showResult) {
          Spacer(modifier = Modifier.height(16.dp))
          Box(
            modifier = Modifier
              .fillMaxWidth()
              .clip(RoundedCornerShape(8.dp))
              .border(1.dp, GymBorderBright, RoundedCornerShape(8.dp))
              .background(GymBackground)
              .padding(14.dp)
          ) {
            Column {
              Text(text = "YOUR BMI", color = GymTextMuted, fontSize = 9.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
              Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(vertical = 4.dp)
              ) {
                Text(
                  text = String.format("%.1f", bmiResult),
                  color = GymRed,
                  fontSize = 32.sp,
                  fontWeight = FontWeight.ExtraBold,
                  fontFamily = FontFamily.SansSerif
                )
                Spacer(modifier = Modifier.width(12.dp))
                Box(
                  modifier = Modifier
                    .clip(RoundedCornerShape(3.dp))
                    .background(calculatedColor.copy(alpha = 0.15f))
                    .border(1.dp, calculatedColor.copy(alpha = 0.35f), RoundedCornerShape(3.dp))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                  Text(
                    text = calculatedStatus.uppercase(),
                    color = calculatedColor,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                  )
                }
              }

              // Progress bar representation
              Spacer(modifier = Modifier.height(10.dp))
              Box(
                modifier = Modifier
                  .fillMaxWidth()
                  .height(6.dp)
                  .clip(CircleShape)
                  .background(GymSurface2)
              ) {
                val percentageFill = (bmiResult / 40.0).toFloat().coerceIn(0.05f, 0.98f)
                Box(
                  modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(percentageFill)
                    .background(calculatedColor)
                )
              }

              Row(
                modifier = Modifier
                  .fillMaxWidth()
                  .padding(top = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween
              ) {
                Text(text = "UNDER", color = GymTextMuted2, fontSize = 8.sp, fontWeight = FontWeight.Bold)
                Text(text = "NORMAL", color = GymTextMuted2, fontSize = 8.sp, fontWeight = FontWeight.Bold)
                Text(text = "OVER", color = GymTextMuted2, fontSize = 8.sp, fontWeight = FontWeight.Bold)
                Text(text = "OBESE", color = GymTextMuted2, fontSize = 8.sp, fontWeight = FontWeight.Bold)
              }

              Divider(color = GymBorder, thickness = 1.dp, modifier = Modifier.padding(vertical = 12.dp))

              Text(
                text = calculatedTip,
                color = GymTextMuted,
                fontSize = 11.sp,
                lineHeight = 16.sp
              )
            }
          }
        }
      }
    }

    Spacer(modifier = Modifier.height(16.dp))

    // Information cards
    Text(
      text = "BMI REFERENCE VALUES",
      color = GymTextMuted,
      fontSize = 10.sp,
      fontWeight = FontWeight.Bold,
      letterSpacing = 2.sp,
      modifier = Modifier.padding(bottom = 8.dp)
    )

    BmiInfoRow(emoji = "📉", label = "Underweight", range = "Below 18.5", color = Color(0xFF60A5FA))
    BmiInfoRow(emoji = "✅", label = "Healthy Range", range = "18.5 – 24.9", color = Color(0xFF4ADE80))
    BmiInfoRow(emoji = "⚠️", label = "Overweight", range = "25.0 – 29.9", color = Color(0xFFFACC15))
    BmiInfoRow(emoji = "🔴", label = "Obese", range = "30.0 and above", color = Color(0xFFEF4444))

    Spacer(modifier = Modifier.height(30.dp))
  }
}

@Composable
fun BmiInfoRow(emoji: String, label: String, range: String, color: Color) {
  Row(
    modifier = Modifier
      .fillMaxWidth()
      .padding(vertical = 4.dp)
      .clip(RoundedCornerShape(8.dp))
      .border(1.dp, GymBorder, RoundedCornerShape(8.dp))
      .background(GymSurface2)
      .padding(12.dp),
    verticalAlignment = Alignment.CenterVertically
  ) {
    Text(text = emoji, fontSize = 18.sp)
    Spacer(modifier = Modifier.width(12.dp))
    Column(modifier = Modifier.weight(1f)) {
      Text(text = label, color = GymTextPrimary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
      Text(text = range, color = GymTextMuted, fontSize = 10.sp)
    }
    Box(
      modifier = Modifier
        .size(8.dp)
        .clip(CircleShape)
        .background(color)
    )
  }
}

@Composable
fun BMRCalculatorView() {
  val context = LocalContext.current
  val scrollState = rememberScrollState()

  var gender by rememberSaveable { mutableStateOf("male") }
  var unitSystem by rememberSaveable { mutableStateOf("metric") }
  var weightInput by rememberSaveable { mutableStateOf("") }
  var heightInput by rememberSaveable { mutableStateOf("") }
  var ageInput by rememberSaveable { mutableStateOf("") }
  var activityLevel by rememberSaveable { mutableStateOf("1.55") }

  var bmrResult by remember { mutableIntStateOf(0) }
  var tdeeResult by remember { mutableIntStateOf(0) }
  var showResult by rememberSaveable { mutableStateOf(false) }

  Column(
    modifier = Modifier
      .fillMaxSize()
      .verticalScroll(scrollState)
      .padding(16.dp)
  ) {
    Box(
      modifier = Modifier
        .fillMaxWidth()
        .clip(RoundedCornerShape(10.dp))
        .border(1.dp, GymBorder, RoundedCornerShape(10.dp))
        .background(GymSurface2)
        .padding(16.dp)
    ) {
      Column {
        Text(
          text = "BMR CALCULATOR",
          color = GymTextPrimary,
          fontSize = 16.sp,
          fontWeight = FontWeight.ExtraBold,
          letterSpacing = 0.5.sp,
          modifier = Modifier.padding(bottom = 12.dp)
        )

        // Gender Buttons
        Text(text = "GENDER", color = GymTextMuted, fontSize = 9.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
            .clip(RoundedCornerShape(4.dp))
            .border(1.dp, GymBorderBright, RoundedCornerShape(4.dp))
        ) {
          Button(
            onClick = { gender = "male" },
            colors = ButtonDefaults.buttonColors(
              containerColor = if (gender == "male") GymRed else Color.Transparent
            ),
            shape = RoundedCornerShape(0.dp),
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(0.dp)
          ) {
            Text(text = "MALE", color = if (gender == "male") Color.White else GymTextMuted, fontSize = 11.sp, fontWeight = FontWeight.Bold)
          }
          Button(
            onClick = { gender = "female" },
            colors = ButtonDefaults.buttonColors(
              containerColor = if (gender == "female") GymRed else Color.Transparent
            ),
            shape = RoundedCornerShape(0.dp),
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(0.dp)
          ) {
            Text(text = "FEMALE", color = if (gender == "female") Color.White else GymTextMuted, fontSize = 11.sp, fontWeight = FontWeight.Bold)
          }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Unit system Buttons
        Text(text = "UNIT SYSTEM", color = GymTextMuted, fontSize = 9.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
            .clip(RoundedCornerShape(4.dp))
            .border(1.dp, GymBorderBright, RoundedCornerShape(4.dp))
        ) {
          Button(
            onClick = { unitSystem = "metric" },
            colors = ButtonDefaults.buttonColors(
              containerColor = if (unitSystem == "metric") GymRed else Color.Transparent
            ),
            shape = RoundedCornerShape(0.dp),
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(0.dp)
          ) {
            Text(text = "METRIC (KG/CM)", color = if (unitSystem == "metric") Color.White else GymTextMuted, fontSize = 11.sp, fontWeight = FontWeight.Bold)
          }
          Button(
            onClick = { unitSystem = "imperial" },
            colors = ButtonDefaults.buttonColors(
              containerColor = if (unitSystem == "imperial") GymRed else Color.Transparent
            ),
            shape = RoundedCornerShape(0.dp),
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(0.dp)
          ) {
            Text(text = "IMPERIAL (LB/IN)", color = if (unitSystem == "imperial") Color.White else GymTextMuted, fontSize = 11.sp, fontWeight = FontWeight.Bold)
          }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Inputs row
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
          Column(modifier = Modifier.weight(1f)) {
            Text(
              text = if (unitSystem == "metric") "WEIGHT (KG)" else "WEIGHT (LB)",
              color = GymTextMuted,
              fontSize = 9.sp,
              fontWeight = FontWeight.Bold,
              letterSpacing = 1.sp
            )
            OutlinedTextField(
              value = weightInput,
              onValueChange = { weightInput = it },
              placeholder = { Text(text = if (unitSystem == "metric") "e.g. 75" else "e.g. 165", fontSize = 13.sp) },
              keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
              colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = GymRed,
                unfocusedBorderColor = GymBorderBright,
                focusedTextColor = GymTextPrimary,
                unfocusedTextColor = GymTextPrimary,
                focusedPlaceholderColor = GymTextMuted2,
                unfocusedPlaceholderColor = GymTextMuted2
              ),
              singleLine = true,
              modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp)
                .testTag("bmr_weight_input")
            )
          }

          Column(modifier = Modifier.weight(1f)) {
            Text(
              text = if (unitSystem == "metric") "HEIGHT (CM)" else "HEIGHT (IN)",
              color = GymTextMuted,
              fontSize = 9.sp,
              fontWeight = FontWeight.Bold,
              letterSpacing = 1.sp
            )
            OutlinedTextField(
              value = heightInput,
              onValueChange = { heightInput = it },
              placeholder = { Text(text = if (unitSystem == "metric") "e.g. 175" else "e.g. 69", fontSize = 13.sp) },
              keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
              colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = GymRed,
                unfocusedBorderColor = GymBorderBright,
                focusedTextColor = GymTextPrimary,
                unfocusedTextColor = GymTextPrimary,
                focusedPlaceholderColor = GymTextMuted2,
                unfocusedPlaceholderColor = GymTextMuted2
              ),
              singleLine = true,
              modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp)
                .testTag("bmr_height_input")
            )
          }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(text = "AGE", color = GymTextMuted, fontSize = 9.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
        OutlinedTextField(
          value = ageInput,
          onValueChange = { ageInput = it },
          placeholder = { Text(text = "e.g. 28", fontSize = 13.sp) },
          keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
          colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = GymRed,
            unfocusedBorderColor = GymBorderBright,
            focusedTextColor = GymTextPrimary,
            unfocusedTextColor = GymTextPrimary,
            focusedPlaceholderColor = GymTextMuted2,
            unfocusedPlaceholderColor = GymTextMuted2
          ),
          singleLine = true,
          modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .testTag("bmr_age_input")
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Activity dropdown emulator (we can use basic outlined row with toggling menu, or simple radio-like choices)
        Text(text = "ACTIVITY LEVEL", color = GymTextMuted, fontSize = 9.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
        val activityOptions = listOf(
          Pair("1.2", "Sedentary (no exercise)"),
          Pair("1.375", "Lightly Active (1-3 days/wk)"),
          Pair("1.55", "Moderately Active (3-5 days/wk)"),
          Pair("1.725", "Very Active (6-7 days/wk)"),
          Pair("1.9", "Super Active (athlete/job)")
        )

        var dropdownExpanded by remember { mutableStateOf(false) }
        val selectedOption = activityOptions.find { it.first == activityLevel } ?: activityOptions[2]

        Box(
          modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
            .clip(RoundedCornerShape(4.dp))
            .border(1.dp, GymBorderBright, RoundedCornerShape(4.dp))
            .background(GymSurface)
            .clickable { dropdownExpanded = !dropdownExpanded }
            .padding(12.dp)
        ) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Text(text = selectedOption.second, color = GymTextPrimary, fontSize = 13.sp, fontWeight = FontWeight.Medium)
            Icon(
              imageVector = if (dropdownExpanded) Icons.Default.ArrowDropUp else Icons.Default.ArrowDropDown,
              contentDescription = "Dropdown icon",
              tint = GymRed
            )
          }

          DropdownMenu(
            expanded = dropdownExpanded,
            onDismissRequest = { dropdownExpanded = false },
            modifier = Modifier
              .fillMaxWidth(0.85f)
              .background(GymSurface2)
              .border(1.dp, GymBorder, RoundedCornerShape(4.dp))
          ) {
            activityOptions.forEach { opt ->
              DropdownMenuItem(
                text = { Text(text = opt.second, color = GymTextPrimary, fontSize = 12.sp) },
                onClick = {
                  activityLevel = opt.first
                  dropdownExpanded = false
                }
              )
            }
          }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
          onClick = {
            var w = weightInput.toDoubleOrNull()
            var h = heightInput.toDoubleOrNull()
            val age = ageInput.toDoubleOrNull()
            val act = activityLevel.toDoubleOrNull() ?: 1.55

            if (w == null || h == null || age == null || w <= 0.0 || h <= 0.0 || age <= 0.0) {
              Toast.makeText(context, "Please enter valid values!", Toast.LENGTH_SHORT).show()
              return@Button
            }

            if (unitSystem == "imperial") {
              w *= 0.453592
              h *= 2.54
            }

            val rawBmr = (10.0 * w) + (6.25 * h) - (5.0 * age) + (if (gender == "male") 5.0 else -161.0)
            bmrResult = Math.round(rawBmr).toInt()
            tdeeResult = Math.round(rawBmr * act).toInt()
            showResult = true
          },
          colors = ButtonDefaults.buttonColors(containerColor = GymRed),
          shape = RoundedCornerShape(4.dp),
          modifier = Modifier
            .fillMaxWidth()
            .testTag("bmr_calculate_button")
        ) {
          Text(text = "CALCULATE BMR & TDEE", fontWeight = FontWeight.ExtraBold, color = Color.White, letterSpacing = 1.sp)
        }

        if (showResult) {
          Spacer(modifier = Modifier.height(16.dp))
          Box(
            modifier = Modifier
              .fillMaxWidth()
              .clip(RoundedCornerShape(8.dp))
              .border(1.dp, GymBorderBright, RoundedCornerShape(8.dp))
              .background(GymBackground)
              .padding(14.dp)
          ) {
            Column {
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
              ) {
                Column {
                  Text(text = "BASAL METABOLIC RATE", color = GymTextMuted, fontSize = 8.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                  Text(
                    text = "$bmrResult kcal",
                    color = GymRed,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.ExtraBold,
                    fontFamily = FontFamily.SansSerif
                  )
                  Text(text = "at complete rest", color = GymTextMuted2, fontSize = 8.sp, fontWeight = FontWeight.Bold)
                }

                Column(horizontalAlignment = Alignment.End) {
                  Text(text = "DAILY NEED (TDEE)", color = GymTextMuted, fontSize = 8.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                  Text(
                    text = "$tdeeResult kcal",
                    color = GymGold,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.ExtraBold,
                    fontFamily = FontFamily.SansSerif
                  )
                  Text(text = "maintenance budget", color = GymTextMuted2, fontSize = 8.sp, fontWeight = FontWeight.Bold)
                }
              }

              Spacer(modifier = Modifier.height(12.dp))
              Divider(color = GymBorder, thickness = 1.dp)
              Spacer(modifier = Modifier.height(10.dp))

              Text(text = "FITNESS GOAL TARGETS", color = GymTextMuted, fontSize = 9.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
              Spacer(modifier = Modifier.height(6.dp))

              Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                GoalTargetCard(title = "Loss (-500)", valStr = "${tdeeResult - 500}", color = Color(0xFF60A5FA), modifier = Modifier.weight(1f))
                GoalTargetCard(title = "Maintain", valStr = "$tdeeResult", color = Color(0xFF4ADE80), modifier = Modifier.weight(1f))
                GoalTargetCard(title = "Gain (+300)", valStr = "${tdeeResult + 300}", color = GymGold, modifier = Modifier.weight(1f))
              }

              Spacer(modifier = Modifier.height(14.dp))

              // Dynamic Daily Macros calculation based on active maintenance
              // Protein: 2g/kg of weight. Fat: 25% of calories. Carbs: remaining calories.
              var kgWeight = weightInput.toDoubleOrNull() ?: 70.0
              if (unitSystem == "imperial") kgWeight *= 0.453592

              val proteinGrams = Math.round(kgWeight * 2.0).toInt().coerceAtLeast(40)
              val fatGrams = Math.round((tdeeResult * 0.25) / 9.0).toInt().coerceAtLeast(30)
              val carbGrams = Math.round((tdeeResult - (proteinGrams * 4.0) - (fatGrams * 9.0)) / 4.0).toInt().coerceAtLeast(50)

              val totCaloriesFromMacros = (proteinGrams * 4) + (carbGrams * 4) + (fatGrams * 9)
              val protPct = Math.round(((proteinGrams * 4.0) / totCaloriesFromMacros) * 100.0).toInt()
              val carbPct = Math.round(((carbGrams * 4.0) / totCaloriesFromMacros) * 100.0).toInt()
              val fatPct = 100 - protPct - carbPct

              Text(text = "DAILY NUTRIENTS SPLIT", color = GymTextMuted, fontSize = 9.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
              Spacer(modifier = Modifier.height(8.dp))

              Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                MacroCard(emoji = "🥩", label = "Protein", value = "${proteinGrams}g", pctStr = "$protPct%", color = Color(0xFFEF4444), modifier = Modifier.weight(1f))
                MacroCard(emoji = "🍚", label = "Carbs", value = "${carbGrams}g", pctStr = "$carbPct%", color = Color(0xFFFACC15), modifier = Modifier.weight(1f))
                MacroCard(emoji = "🥑", label = "Fat", value = "${fatGrams}g", pctStr = "$fatPct%", color = Color(0xFF4ADE80), modifier = Modifier.weight(1f))
              }

              // Custom three-segment horizontal progress bar reflecting ratio
              Spacer(modifier = Modifier.height(10.dp))
              Row(
                modifier = Modifier
                  .fillMaxWidth()
                  .height(6.dp)
                  .clip(CircleShape)
              ) {
                Box(modifier = Modifier
                  .weight(protPct.toFloat())
                  .fillMaxHeight()
                  .background(Color(0xFFEF4444)))
                Box(modifier = Modifier
                  .weight(carbPct.toFloat())
                  .fillMaxHeight()
                  .background(Color(0xFFFACC15)))
                Box(modifier = Modifier
                  .weight(fatPct.toFloat())
                  .fillMaxHeight()
                  .background(Color(0xFF4ADE80)))
              }

              Spacer(modifier = Modifier.height(8.dp))

              Text(
                text = "Based on your calculations, aim for $proteinGrams grams of high-quality protein to support muscle synthesis and prevent tissue breakdown during exercise.",
                color = GymTextMuted,
                fontSize = 11.sp,
                lineHeight = 16.sp
              )
            }
          }
        }
      }
    }

    Spacer(modifier = Modifier.height(30.dp))
  }
}

@Composable
fun GoalTargetCard(title: String, valStr: String, color: Color, modifier: Modifier = Modifier) {
  Column(
    modifier = modifier
      .clip(RoundedCornerShape(6.dp))
      .border(1.dp, GymBorder, RoundedCornerShape(6.dp))
      .background(GymSurface2)
      .padding(8.dp),
    horizontalAlignment = Alignment.CenterHorizontally
  ) {
    Text(text = title.uppercase(), color = color, fontSize = 8.sp, fontWeight = FontWeight.Bold, letterSpacing = 0.5.sp)
    Text(text = valStr, color = GymTextPrimary, fontSize = 14.sp, fontWeight = FontWeight.ExtraBold, fontFamily = FontFamily.SansSerif)
    Text(text = "cal/day", color = GymTextMuted2, fontSize = 7.sp, fontWeight = FontWeight.Bold)
  }
}

@Composable
fun MacroCard(emoji: String, label: String, value: String, pctStr: String, color: Color, modifier: Modifier = Modifier) {
  Column(
    modifier = modifier
      .clip(RoundedCornerShape(6.dp))
      .border(1.dp, color.copy(alpha = 0.15f), RoundedCornerShape(6.dp))
      .background(GymSurface2)
      .padding(8.dp),
    horizontalAlignment = Alignment.CenterHorizontally
  ) {
    Text(text = emoji, fontSize = 16.sp)
    Text(text = value, color = color, fontSize = 15.sp, fontWeight = FontWeight.ExtraBold, fontFamily = FontFamily.SansSerif)
    Text(text = label, color = GymTextPrimary, fontSize = 9.sp, fontWeight = FontWeight.Bold)
    Text(text = pctStr, color = GymTextMuted2, fontSize = 8.sp, fontWeight = FontWeight.Bold)
  }
}

// ==========================================
// ═══ MORE PAGE ═══
// ==========================================
@Composable
fun PageMore() {
  val context = LocalContext.current
  val scrollState = rememberScrollState()

  // Slider indices for Events & Achievements
  var eventIndex by remember { mutableIntStateOf(0) }
  var achieveIndex by remember { mutableIntStateOf(0) }

  LaunchedEffect(Unit) {
    while (true) {
      delay(3800)
      eventIndex = (eventIndex + 1) % 3
      achieveIndex = (achieveIndex + 1) % 3
    }
  }

  Column(
    modifier = Modifier
      .fillMaxSize()
      .verticalScroll(scrollState)
  ) {
    // Page Header
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .background(Brush.verticalGradient(listOf(GymRed.copy(alpha = 0.04f), Color.Transparent)))
        .padding(top = 20.dp, start = 16.dp, end = 16.dp, bottom = 12.dp)
    ) {
      Text(
        text = "Explore",
        color = GymRed,
        fontSize = 11.sp,
        fontWeight = FontWeight.Bold,
        letterSpacing = 2.sp
      )
      Text(
        text = "MORE",
        color = GymTextPrimary,
        fontSize = 22.sp,
        fontWeight = FontWeight.ExtraBold,
        letterSpacing = 1.sp,
        modifier = Modifier.padding(top = 2.dp)
      )
    }

    // --- Contact Card Block ---
    Text(
      text = "  Get In Touch",
      color = GymRed,
      fontSize = 10.sp,
      fontWeight = FontWeight.Bold,
      letterSpacing = 2.sp,
      modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
    )

    Column(
      modifier = Modifier
        .padding(horizontal = 16.dp)
        .fillMaxWidth()
        .clip(RoundedCornerShape(10.dp))
        .border(1.dp, GymBorder, RoundedCornerShape(10.dp))
        .background(GymSurface2)
    ) {
      ContactRowItem(
        icon = Icons.Default.Phone,
        label = "Phone",
        value = "+91 9609832596"
      ) {
        val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:+919609832596"))
        context.startActivity(intent)
      }

      Divider(color = GymBorder, thickness = 1.dp)

      ContactRowItem(
        icon = Icons.Default.Email,
        label = "Email",
        value = "mahakalgymandfitnessstudio8@gmail.com"
      ) {
        val intent = Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:mahakalgymandfitnessstudio8@gmail.com"))
        context.startActivity(intent)
      }

      Divider(color = GymBorder, thickness = 1.dp)

      ContactRowItem(
        icon = Icons.Default.Language,
        label = "Website",
        value = "mahakal0808.github.io"
      ) {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://mahakal0808.github.io/GYM-FITNESS-STUDIO/"))
        context.startActivity(intent)
      }
    }

    Spacer(modifier = Modifier.height(14.dp))

    // --- Social Media Row ---
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 16.dp),
      horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
      SocialBtn(
        emoji = "📸",
        label = "Instagram",
        modifier = Modifier.weight(1f)
      ) {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.instagram.com/mahakal_gym_1?igsh=ejVyZ3RleWh4Y2Y4"))
        context.startActivity(intent)
      }

      SocialBtn(
        emoji = "👥",
        label = "Facebook",
        modifier = Modifier.weight(1f)
      ) {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.facebook.com/share/1EAEfZFPGP/"))
        context.startActivity(intent)
      }
    }

    Spacer(modifier = Modifier.height(20.dp))

    // --- Location Map Info ---
    Text(
      text = "  Find Us",
      color = GymRed,
      fontSize = 10.sp,
      fontWeight = FontWeight.Bold,
      letterSpacing = 2.sp,
      modifier = Modifier.padding(horizontal = 16.dp)
    )
    Text(
      text = "  LOCATION",
      color = GymTextPrimary,
      fontSize = 18.sp,
      fontWeight = FontWeight.ExtraBold,
      letterSpacing = 1.sp,
      modifier = Modifier.padding(horizontal = 16.dp, vertical = 2.dp)
    )

    Spacer(modifier = Modifier.height(8.dp))

    Box(
      modifier = Modifier
        .padding(horizontal = 16.dp)
        .fillMaxWidth()
        .clip(RoundedCornerShape(10.dp))
        .border(1.dp, GymBorderBright, RoundedCornerShape(10.dp))
        .background(GymSurface2)
        .padding(16.dp)
    ) {
      Column {
        Text(text = "MAHAKAL GYM AND FITNESS STUDIO", color = GymTextPrimary, fontSize = 13.sp, fontWeight = FontWeight.Bold, letterSpacing = 0.5.sp)
        Text(text = "Ghatal, West Bengal, India", color = GymTextMuted, fontSize = 11.sp, modifier = Modifier.padding(vertical = 2.dp))
        Text(text = "Near the core business circle, with full 3000 sq ft dedicated free parking for members' two-wheelers & four-wheelers.", color = GymTextMuted, fontSize = 11.sp, lineHeight = 16.sp)

        Spacer(modifier = Modifier.height(14.dp))

        Button(
          onClick = {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://maps.app.goo.gl/hX3LC9vCuxfoXqSR8"))
            context.startActivity(intent)
          },
          colors = ButtonDefaults.buttonColors(containerColor = GymRed),
          shape = RoundedCornerShape(4.dp),
          modifier = Modifier.fillMaxWidth()
        ) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(imageVector = Icons.Default.Map, contentDescription = "Map icon", modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text(text = "OPEN IN GOOGLE MAPS", color = Color.White, fontWeight = FontWeight.ExtraBold, fontSize = 11.sp, letterSpacing = 0.5.sp)
          }
        }
      }
    }

    Spacer(modifier = Modifier.height(20.dp))
    Divider(color = GymBorder, thickness = 1.dp)

    // --- Championship Events Carousel ---
    Column(modifier = Modifier.padding(top = 16.dp)) {
      Text(
        text = "  Events",
        color = GymRed,
        fontSize = 10.sp,
        fontWeight = FontWeight.Bold,
        letterSpacing = 2.sp,
        modifier = Modifier.padding(horizontal = 16.dp)
      )
      Text(
        text = "  2ND INTER GYM CHAMPIONSHIP 2026",
        color = GymTextPrimary,
        fontSize = 18.sp,
        fontWeight = FontWeight.ExtraBold,
        letterSpacing = 1.sp,
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 2.dp)
      )

      Spacer(modifier = Modifier.height(8.dp))

      val events = listOf(
        Pair("HEAVYWEIGHT BENCH SHOWDOWN", "Powerlifting elite meet-up on Jan 14th."),
        Pair("PHYSIQUE SYMPOSIUM 2026", "Classic bodybuilding championship display."),
        Pair("KARATE & NUNCHAKU EXHIBITION", "Elite defensive skills demonstration.")
      )

      val activeEv = events[eventIndex]

      Box(
        modifier = Modifier
          .padding(horizontal = 16.dp)
          .fillMaxWidth()
          .height(140.dp)
          .clip(RoundedCornerShape(8.dp))
          .border(1.dp, GymBorder, RoundedCornerShape(8.dp))
          .background(Brush.verticalGradient(listOf(GymSurface3, GymSurface2)))
          .padding(16.dp),
        contentAlignment = Alignment.Center
      ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
          Text(text = "🏆 EVENT ${eventIndex + 1}", color = GymGold, fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 2.sp)
          Spacer(modifier = Modifier.height(6.dp))
          Text(text = activeEv.first, color = GymTextPrimary, fontSize = 18.sp, fontWeight = FontWeight.ExtraBold, textAlign = TextAlign.Center)
          Text(text = activeEv.second, color = GymTextMuted, fontSize = 11.sp, textAlign = TextAlign.Center, modifier = Modifier.padding(top = 2.dp))
        }
      }

      // dots
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .padding(vertical = 10.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
      ) {
        for (i in 0..2) {
          Box(
            modifier = Modifier
              .padding(horizontal = 3.dp)
              .size(if (i == eventIndex) 8.dp else 6.dp)
              .clip(CircleShape)
              .background(if (i == eventIndex) GymRed else GymTextMuted2)
          )
        }
      }
    }

    // --- Highlights / Achievements Carousel ---
    Column(modifier = Modifier.padding(top = 10.dp)) {
      Text(
        text = "  Highlights",
        color = GymRed,
        fontSize = 10.sp,
        fontWeight = FontWeight.Bold,
        letterSpacing = 2.sp,
        modifier = Modifier.padding(horizontal = 16.dp)
      )
      Text(
        text = "  MEMBER ACHIEVEMENTS",
        color = GymTextPrimary,
        fontSize = 18.sp,
        fontWeight = FontWeight.ExtraBold,
        letterSpacing = 1.sp,
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 2.dp)
      )

      Spacer(modifier = Modifier.height(8.dp))

      val achieves = listOf(
        Pair("GOLD MEDAL - STATE POWERLIFTING", "Won by Rudranil Jana in 85kg class."),
        Pair("BEST TRANFORMATION AWARD", "Won by Sisir Bag - Lost 24kg in 6 months."),
        Pair("YOGA EXCELLENCE CERTIFICATE", "Awarded to Ankita Manna's top studio class.")
      )

      val activeAch = achieves[achieveIndex]

      Box(
        modifier = Modifier
          .padding(horizontal = 16.dp)
          .fillMaxWidth()
          .height(140.dp)
          .clip(RoundedCornerShape(8.dp))
          .border(1.dp, GymBorder, RoundedCornerShape(8.dp))
          .background(Brush.verticalGradient(listOf(GymSurface3, GymSurface2)))
          .padding(16.dp),
        contentAlignment = Alignment.Center
      ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
          Text(text = "🎖️ HIGHLIGHT ${achieveIndex + 1}", color = GymGold, fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 2.sp)
          Spacer(modifier = Modifier.height(6.dp))
          Text(text = activeAch.first, color = GymTextPrimary, fontSize = 18.sp, fontWeight = FontWeight.ExtraBold, textAlign = TextAlign.Center)
          Text(text = activeAch.second, color = GymTextMuted, fontSize = 11.sp, textAlign = TextAlign.Center, modifier = Modifier.padding(top = 2.dp))
        }
      }

      // dots
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .padding(vertical = 10.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
      ) {
        for (i in 0..2) {
          Box(
            modifier = Modifier
              .padding(horizontal = 3.dp)
              .size(if (i == achieveIndex) 8.dp else 6.dp)
              .clip(CircleShape)
              .background(if (i == achieveIndex) GymRed else GymTextMuted2)
          )
        }
      }
    }

    Spacer(modifier = Modifier.height(10.dp))

    // --- Gym Hours ---
    Box(
      modifier = Modifier
        .padding(horizontal = 16.dp)
        .fillMaxWidth()
        .clip(RoundedCornerShape(10.dp))
        .border(1.dp, GymBorder, RoundedCornerShape(10.dp))
        .background(GymSurface2)
        .padding(16.dp)
    ) {
      Column {
        Text(text = "GYM HOURS", color = GymTextPrimary, fontSize = 13.sp, fontWeight = FontWeight.Bold, letterSpacing = 0.5.sp)
        Spacer(modifier = Modifier.height(12.dp))

        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Text(text = "🌅", fontSize = 18.sp)
            Spacer(modifier = Modifier.width(10.dp))
            Text(text = "Morning Session", color = GymTextPrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
          }
          Text(text = "5 AM – 1 PM", color = GymRed, fontSize = 13.sp, fontWeight = FontWeight.ExtraBold)
        }

        Spacer(modifier = Modifier.height(10.dp))
        Divider(color = GymBorder, thickness = 1.dp)
        Spacer(modifier = Modifier.height(10.dp))

        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Text(text = "🌆", fontSize = 18.sp)
            Spacer(modifier = Modifier.width(10.dp))
            Text(text = "Evening Session", color = GymTextPrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
          }
          Text(text = "3 PM – 9:30 PM", color = GymRed, fontSize = 13.sp, fontWeight = FontWeight.ExtraBold)
        }
      }
    }

    Spacer(modifier = Modifier.height(30.dp))
  }
}

@Composable
fun ContactRowItem(icon: ImageVector, label: String, value: String, onClick: () -> Unit) {
  Row(
    modifier = Modifier
      .fillMaxWidth()
      .clickable(onClick = onClick)
      .padding(16.dp),
    verticalAlignment = Alignment.CenterVertically
  ) {
    Box(
      modifier = Modifier
        .size(34.dp)
        .clip(RoundedCornerShape(6.dp))
        .background(GymRed.copy(alpha = 0.1f)),
      contentAlignment = Alignment.Center
    ) {
      Icon(
        imageVector = icon,
        contentDescription = label,
        tint = GymRed,
        modifier = Modifier.size(16.dp)
      )
    }

    Spacer(modifier = Modifier.width(14.dp))

    Column(modifier = Modifier.weight(1f)) {
      Text(text = label.uppercase(), color = GymTextMuted, fontSize = 8.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
      Text(text = value, color = GymTextPrimary, fontSize = 12.sp, fontWeight = FontWeight.Medium, maxLines = 1, overflow = TextOverflow.Ellipsis)
    }

    Icon(
      imageVector = Icons.Default.ChevronRight,
      contentDescription = "Arrow",
      tint = GymTextMuted2,
      modifier = Modifier.size(16.dp)
    )
  }
}

@Composable
fun SocialBtn(emoji: String, label: String, modifier: Modifier = Modifier, onClick: () -> Unit) {
  Box(
    modifier = modifier
      .clip(RoundedCornerShape(8.dp))
      .border(1.dp, GymBorderBright, RoundedCornerShape(8.dp))
      .background(GymSurface)
      .clickable(onClick = onClick)
      .padding(vertical = 12.dp),
    contentAlignment = Alignment.Center
  ) {
    Row(verticalAlignment = Alignment.CenterVertically) {
      Text(text = emoji, fontSize = 16.sp)
      Spacer(modifier = Modifier.width(8.dp))
      Text(text = label.uppercase(), color = GymTextMuted, fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 0.5.sp)
    }
  }
}
