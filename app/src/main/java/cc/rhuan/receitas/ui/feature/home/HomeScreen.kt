package cc.rhuan.receitas.ui.feature.home

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cc.rhuan.receitas.R
import cc.rhuan.receitas.ui.theme.ReceitasTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen() {
    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior(
        state = rememberTopAppBarState()
    )
    Scaffold(
        topBar = {
            TopBar(
                scrollBehavior = scrollBehavior,

            )
        }
    ) { paddingValues ->
        HomeContent(
            modifier = Modifier.padding(paddingValues)
        )
    }
}

@Composable
fun HomeContent(modifier: Modifier = Modifier)  {

}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun TopBar(
    modifier: Modifier = Modifier,
    scrollBehavior: TopAppBarScrollBehavior
) {
    TopAppBar(
        modifier = modifier,
        scrollBehavior = scrollBehavior,
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        title = {
            Text(
                text = "Receitas",
                color = MaterialTheme.colorScheme.onBackground.copy(0.7f),
                fontSize = 17.sp
            )
        },
        navigationIcon = {
            Icon(
                painter = painterResource(
                    id = R.drawable.ic_hamburger_menu
                ),
                contentDescription = null,
                modifier = Modifier
                    .padding(start = 16.dp, end= 8.dp)
            )
        }
    )
}

@Preview
@Composable
private fun HomePreview() {
    ReceitasTheme() {
        HomeScreen()
    }
}