package com.agonylua.smarthome.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.agonylua.smarthome.R;
import com.agonylua.smarthome.database.entity.Rules;

import java.util.ArrayList;
import java.util.List;

public class SmartAdapter extends RecyclerView.Adapter<SmartAdapter.SceneViewHolder> {

    private final List<Rules> sceneList = new ArrayList<>();
    private OnSceneActionListener listener;
    // 记录当前处于删除确认状态的规则ID，只允许一个规则显示删除界面
    private String currentDeletingRuleId = null;

    public void setOnItemClickListener(SmartAdapter.OnSceneActionListener listener) {
        this.listener = listener;
    }

    public void submitList(List<Rules> newList) {
        DiffUtil.DiffResult diffResult = DiffUtil.calculateDiff(new DiffUtil.Callback() {
            @Override
            public int getOldListSize() {
                return sceneList.size();
            }

            @Override
            public int getNewListSize() {
                return newList.size();
            }

            @Override
            public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
                Rules oldRule = sceneList.get(oldItemPosition);
                Rules newRule = newList.get(newItemPosition);
                return oldRule.getRuleId().equals(newRule.getRuleId());
            }

            @Override
            public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
                Rules oldRule = sceneList.get(oldItemPosition);
                Rules newRule = newList.get(newItemPosition);
                return oldRule.equals(newRule);
            }
        });

        this.sceneList.clear();
        this.sceneList.addAll(newList);
        diffResult.dispatchUpdatesTo(this);
    }

    @NonNull
    @Override
    public SceneViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_smart_automation, parent, false);
        return new SceneViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SceneViewHolder holder, int position) {
        Rules rule = sceneList.get(position);
        holder.tvName.setText(rule.getRuleName());
        holder.tvType.setText(rule.getConditionType());

        // 判断当前规则是否处于删除确认状态
        boolean isDeleting = rule.getRuleId() != null && rule.getRuleId().equals(currentDeletingRuleId);
        holder.clDeleteOverlay.setVisibility(isDeleting ? View.VISIBLE : View.GONE);

        // 自定义长按逻辑
        holder.itemView.setOnTouchListener((v, event) -> {
            switch (event.getAction()) {
                case android.view.MotionEvent.ACTION_DOWN:
                    // 如果点击的不是当前正在显示遮罩的卡片，清除其他的遮罩状态
                    if (currentDeletingRuleId != null && !rule.getRuleId().equals(currentDeletingRuleId)) {
                        clearDeleteMode();
                    }

                    if (holder.longPressRunnable != null) {
                        v.removeCallbacks(holder.longPressRunnable);
                    }
                    holder.longPressRunnable = () -> {
                        String oldDeletingId = currentDeletingRuleId;
                        currentDeletingRuleId = rule.getRuleId();
                        // 刷新旧的打开项将其关闭
                        if (oldDeletingId != null && !oldDeletingId.equals(currentDeletingRuleId)) {
                            for (int i = 0; i < sceneList.size(); i++) {
                                String rId = sceneList.get(i).getRuleId();
                                if (rId != null && rId.equals(oldDeletingId)) {
                                    notifyItemChanged(i);
                                    break;
                                }
                            }
                        }
                        // 刷新当前项以显示遮罩
                        notifyItemChanged(holder.getAdapterPosition());
                        // 可选用震动提醒长按成功
                        // v.performHapticFeedback(android.view.HapticFeedbackConstants.LONG_PRESS);
                    };
                    v.postDelayed(holder.longPressRunnable, 1000); // 3秒长按
                    break;
                case android.view.MotionEvent.ACTION_UP:
                case android.view.MotionEvent.ACTION_CANCEL:
                case android.view.MotionEvent.ACTION_MOVE:
                    // 手指抬起、移动过大或被取消时，若没到1秒则取消显示
                    if (event.getAction() != android.view.MotionEvent.ACTION_MOVE ||
                            (event.getAction() == android.view.MotionEvent.ACTION_MOVE && event.getHistorySize() > 0)) {
                        if (holder.longPressRunnable != null) {
                            v.removeCallbacks(holder.longPressRunnable);
                        }
                    }
                    break;
            }
            // 不消费触摸事件，继续向下传递给点击事件
            return false;
        });

        // 遮罩存在拦截点击，使得点击卡片其他地方取消删除
        holder.clDeleteOverlay.setOnClickListener(v -> {
            currentDeletingRuleId = null;
            notifyItemChanged(holder.getAdapterPosition());
        });

        // 点击确认删除按钮
        holder.btnDeleteConfirm.setOnClickListener(v -> {
            currentDeletingRuleId = null;
            notifyItemChanged(holder.getAdapterPosition());
            if (listener != null) {
                listener.onDeleteRule(rule);
            }
        });

        // 根据场景类型区分右侧的交互控件
        switch (rule.getConditionType()) {
            case "SENSOR": {
                String condition = rule.getConditionProperty() +
                        " " +
                        rule.getConditionOperator() +
                        " " +
                        rule.getConditionValue();
                holder.tvCondition.setText(condition);

                String action = rule.getActionDeviceSn() +
                        Parsing(rule.getActionCommand()) +
                        "设置为" +
                        rule.getActionPayload();
                holder.tvAction.setText(action);
                break;
            }
            case "TIMER": {
                String condition = "每天" +
                        rule.getConditionValue();
                holder.tvCondition.setText(condition);

                String action = rule.getActionDeviceSn() +
                        Parsing(rule.getActionCommand()) +
                        "设置为" +
                        rule.getActionPayload();
                holder.tvAction.setText(action);
                break;
            }
            case "STATUS": {
                String condition = rule.getActionDeviceSn() +
                        Parsing(rule.getConditionProperty()) +
                        " " +
                        rule.getConditionOperator() +
                        " " +
                        rule.getConditionValue();
                holder.tvCondition.setText(condition);

                String action = rule.getActionDeviceSn() +
                        Parsing(rule.getActionCommand()) +
                        "设置为" +
                        rule.getActionPayload();
                holder.tvAction.setText(action);
                break;
            }
        }

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onExecutePreset(rule);
            }
        });
    }

    @Override
    public int getItemCount() {
        return sceneList.size();
    }

    // 暴露方法：清除所有的删除确认遮罩状态（用于点击其他地方时调用）
    public void clearDeleteMode() {
        if (currentDeletingRuleId != null) {
            String tempId = currentDeletingRuleId;
            currentDeletingRuleId = null;
            for (int i = 0; i < sceneList.size(); i++) {
                String rId = sceneList.get(i).getRuleId();
                if (rId != null && rId.equals(tempId)) {
                    notifyItemChanged(i);
                    break;
                }
            }
        }
    }

    public String Parsing(String cmd) {
        if (cmd.equals("status")) {
            return "状态";
        } else if (cmd.equals("mode")) {
            return "模式";
        }
        return cmd;
    }

    public interface OnSceneActionListener {
        void onExecutePreset(Rules rule);

        void onToggleCustomRule(Rules rule, boolean isChecked);

        // 新增删除回调
        void onDeleteRule(Rules rule);
    }

    public static class SceneViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvType, tvCondition, tvAction;
        View clDeleteOverlay;
        View btnDeleteConfirm;
        Runnable longPressRunnable;

        public SceneViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tv_rule_name);
            tvType = itemView.findViewById(R.id.rule_type);
            tvCondition = itemView.findViewById(R.id.rule_condition);
            tvAction = itemView.findViewById(R.id.tv_action);
            clDeleteOverlay = itemView.findViewById(R.id.cl_delete_overlay);
            btnDeleteConfirm = itemView.findViewById(R.id.btn_delete_confirm);
        }
    }
}