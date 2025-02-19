package selector

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import item.SileroModelTypeItem

@Composable
fun SileroModelTypeItemSelector(
    modifier: Modifier,
    selectedSileroModelTypeItem: SileroModelTypeItem,
    selectSileroModelTypeItem: (SileroModelTypeItem) -> Unit,
) {
    Selector(
        modifier = modifier,
        items = SileroModelTypeItem.entries.map { modelType ->
            when (modelType) {
                SileroModelTypeItem.SMALL_FAST -> "Small fast"

                SileroModelTypeItem.SMALL_SLOW -> "Small slow"

                SileroModelTypeItem.LARGE_FAST -> "Large fast"
            }
        },
        selectedIndex = SileroModelTypeItem.entries.indexOf(selectedSileroModelTypeItem),
        selectIndex = { index -> selectSileroModelTypeItem(SileroModelTypeItem.entries.elementAt(index)) }
    )
}