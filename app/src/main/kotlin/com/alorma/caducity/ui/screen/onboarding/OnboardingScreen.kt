package com.alorma.caducity.ui.screen.onboarding

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.alorma.caducity.ui.screen.onboarding.components.OnboardingButtons
import com.alorma.caducity.ui.screen.onboarding.components.OnboardingPagerIndicator
import com.alorma.caducity.ui.screen.onboarding.pages.DisclaimerOnboardingPage
import com.alorma.caducity.ui.screen.onboarding.pages.FeaturesOnboardingPage
import com.alorma.caducity.ui.screen.onboarding.pages.PermissionsOnboardingPage
import com.alorma.caducity.ui.screen.onboarding.pages.TutorialOnboardingPage
import com.alorma.caducity.ui.screen.onboarding.pages.WelcomeOnboardingPage
import kotlinx.coroutines.launch
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun OnboardingScreen(
  onCompleted: () -> Unit,
  modifier: Modifier = Modifier,
  viewModel: OnboardingViewModel = koinViewModel(),
) {
  val state by viewModel.state.collectAsState()
  val pagerState = rememberPagerState(pageCount = { state.totalPages })
  val coroutineScope = rememberCoroutineScope()

  // Sync ViewModel state with pager state
  LaunchedEffect(state.currentPage) {
    if (pagerState.currentPage != state.currentPage) {
      pagerState.animateScrollToPage(state.currentPage)
    }
  }

  LaunchedEffect(pagerState.currentPage) {
    if (state.currentPage != pagerState.currentPage) {
      viewModel.goToPage(pagerState.currentPage)
    }
  }

  // Handle completion
  LaunchedEffect(state.isCompleted) {
    if (state.isCompleted) {
      onCompleted()
    }
  }

  Box(
    modifier = modifier.fillMaxSize(),
  ) {
    Column(
      modifier = Modifier.fillMaxSize(),
    ) {
      // Pager
      HorizontalPager(
        state = pagerState,
        modifier = Modifier
          .fillMaxWidth()
          .weight(1f),
      ) { page ->
        when (page) {
          0 -> WelcomeOnboardingPage()
          1 -> FeaturesOnboardingPage()
          2 -> PermissionsOnboardingPage()
          3 -> TutorialOnboardingPage()
          4 -> DisclaimerOnboardingPage()
        }
      }

      // Page Indicator
      OnboardingPagerIndicator(
        pageCount = state.totalPages,
        currentPage = state.currentPage,
        modifier = Modifier
          .align(Alignment.CenterHorizontally)
          .padding(16.dp),
      )

      // Navigation Buttons
      OnboardingButtons(
        canSkip = state.canSkip,
        isLastPage = state.isLastPage,
        isFirstPage = state.isFirstPage,
        onNext = {
          coroutineScope.launch {
            viewModel.nextPage()
          }
        },
        onPrevious = {
          coroutineScope.launch {
            viewModel.previousPage()
          }
        },
        onSkip = { viewModel.skipOnboarding() },
        onComplete = { viewModel.acceptDisclaimer() },
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 24.dp, vertical = 16.dp),
      )
    }
  }
}
