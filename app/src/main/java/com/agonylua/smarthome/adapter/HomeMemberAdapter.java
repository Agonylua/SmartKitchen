package com.agonylua.smarthome.adapter;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.HorizontalScrollView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.agonylua.smarthome.R;
import com.bumptech.glide.Glide;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.imageview.ShapeableImageView;

import java.util.ArrayList;
import java.util.List;

public class HomeMemberAdapter extends RecyclerView.Adapter<HomeMemberAdapter.MemberViewHolder> {

    private List<HomeMember> memberList = new ArrayList<>();
    private boolean isCurrentUserOwner = false;
    private OnMemberDeleteListener deleteListener;
    // 记录当前处于打开状态的侧滑菜单
    private HorizontalScrollView openedSwipeView = null;

    private void closeOpenedSwipe() {
        if (openedSwipeView != null) {
            openedSwipeView.smoothScrollTo(0, 0);
            openedSwipeView = null;
        }
    }

    @Override
    public void onAttachedToRecyclerView(@NonNull RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);

        // 全局拦截 RecyclerView 触摸事件
        recyclerView.addOnItemTouchListener(new RecyclerView.OnItemTouchListener() {
            @Override
            public boolean onInterceptTouchEvent(@NonNull RecyclerView rv, @NonNull MotionEvent e) {
                if (e.getAction() == MotionEvent.ACTION_DOWN) {
                    if (openedSwipeView != null) {
                        int[] screenPos = new int[2];
                        openedSwipeView.getLocationOnScreen(screenPos);
                        int left = screenPos[0];
                        int top = screenPos[1];
                        int right = left + openedSwipeView.getWidth();
                        int bottom = top + openedSwipeView.getHeight();

                        int x = (int) e.getRawX();
                        int y = (int) e.getRawY();

                        // 1. 若触摸点不在当前打开的卡片范围内，则收起它并拦截本次事件
                        if (x < left || x > right || y < top || y > bottom) {
                            closeOpenedSwipe();
                            return true;
                        } else {
                            // 2. 触摸点在当前卡片范围内，但检查是否点击在移除按钮上
                            View deleteContainer = openedSwipeView.findViewById(R.id.fl_delete_container);
                            if (deleteContainer != null && deleteContainer.getVisibility() == View.VISIBLE) {
                                int[] delPos = new int[2];
                                deleteContainer.getLocationOnScreen(delPos);
                                int dLeft = delPos[0];
                                int dTop = delPos[1];
                                int dRight = dLeft + deleteContainer.getWidth();
                                int dBottom = dTop + deleteContainer.getHeight();
                                // 如果没点在删除按钮上，也收起并拦截
                                if (x < dLeft || x > dRight || y < dTop || y > dBottom) {
                                    closeOpenedSwipe();
                                    return true;
                                }
                            }
                        }
                    }
                }
                return false;
            }

            @Override
            public void onTouchEvent(@NonNull RecyclerView rv, @NonNull MotionEvent e) {
            }

            @Override
            public void onRequestDisallowInterceptTouchEvent(boolean disallowIntercept) {
            }
        });

        // 列表滚动时关闭打开的菜单
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                if (newState == RecyclerView.SCROLL_STATE_DRAGGING) {
                    closeOpenedSwipe();
                }
            }
        });
    }

    public void setOnMemberDeleteListener(OnMemberDeleteListener listener) {
        this.deleteListener = listener;
    }

    public void setIsCurrentUserOwner(boolean isOwner) {
        this.isCurrentUserOwner = isOwner;
        notifyDataSetChanged();
    }

    public void submitList(ArrayList<HomeMember> list) {
        this.memberList.clear();
        this.memberList.addAll(list);
        notifyDataSetChanged();
    }

    public List<HomeMember> getList() {
        return memberList;
    }

    public void removeItem(int position) {
        memberList.remove(position);
        notifyItemRemoved(position);
    }

    @SuppressLint("ClickableViewAccessibility")
    @NonNull
    @Override
    public MemberViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_home_member, parent, false);
        MemberViewHolder holder = new MemberViewHolder(view);

        // 核心技术点 1：将内部的显示容器撑开，使其与外层 RecyclerView 宽度完全一致
        ViewGroup.LayoutParams lp = holder.flMainContent.getLayoutParams();
        lp.width = parent.getWidth();
        holder.flMainContent.setLayoutParams(lp);

        // 核心技术点 2：监听触摸松开事件，实现自动吸附(Snap)动画，防止停留在半开半闭状态
        holder.hsvSwipe.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                // 滑动当前项时，如果其他项是打开状态，则将其他项收起
                if (openedSwipeView != null && openedSwipeView != holder.hsvSwipe) {
                    openedSwipeView.smoothScrollTo(0, 0);
                    openedSwipeView = null;
                }
            } else if (event.getAction() == MotionEvent.ACTION_UP || event.getAction() == MotionEvent.ACTION_CANCEL) {
                int scrollX = holder.hsvSwipe.getScrollX();
                int btnWidth = holder.flDeleteContainer.getWidth();

                // 如果滑动距离超过按钮的一半，则自动平滑拉开；否则缩回隐藏
                if (scrollX > btnWidth / 2) {
                    holder.hsvSwipe.smoothScrollTo(btnWidth, 0);
                    openedSwipeView = holder.hsvSwipe; // 记录当前打开的菜单
                } else {
                    holder.hsvSwipe.smoothScrollTo(0, 0);
                    if (openedSwipeView == holder.hsvSwipe) {
                        openedSwipeView = null;
                    }
                }
                return true; // 拦截默认的 Fling(快速滑动) 行为
            }
            return false;
        });

        // 点击其他部分（主内容区域）时，如果有打开的菜单，则自动收起
        holder.flMainContent.setOnClickListener(v -> {
            if (openedSwipeView != null) {
                openedSwipeView.smoothScrollTo(0, 0);
                openedSwipeView = null;
            }
        });

        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull MemberViewHolder holder, int position) {
        HomeMember member = memberList.get(position);

        // 【重要】RecyclerView 会复用 View，必须立即将滑动视图归零，防止出现被复用的卡片是“拉开”的状态
        holder.hsvSwipe.scrollTo(0, 0);
        if (openedSwipeView == holder.hsvSwipe) {
            openedSwipeView = null;
        }

        holder.tvUsername.setText(member.username);
        holder.tvUserId.setText(member.userId);
        holder.tagOwner.setVisibility(member.isOwner ? View.VISIBLE : View.GONE);
        holder.tagMe.setVisibility(member.isMe ? View.VISIBLE : View.GONE);

        Glide.with(holder.itemView.getContext())
                .load(member.avatarUrl)
                .placeholder(R.drawable.login_user) // 滑动时先显示默认图
                .fallback(R.drawable.login_user)
                .error(R.drawable.ic_launcher_foreground)
                .circleCrop()
                .into(holder.avatarImageView);

        if (!member.isOwner && !member.isMe) {
            holder.tvUsername.setTextColor(0xFF4B5563);
        } else {
            holder.tvUsername.setTextColor(0xFF1F2937);
        }

        // 权限控制：只有你是户主，且当前卡片不是户主本人时，才显示侧滑菜单
        if (isCurrentUserOwner && !member.isOwner) {
            holder.flDeleteContainer.setVisibility(View.VISIBLE);

            // 绑定移出按钮的点击事件
            holder.cardDelete.setOnClickListener(v -> {
                if (deleteListener != null) {
                    deleteListener.onDeleteClick(holder.getAdapterPosition(), member);
                    holder.hsvSwipe.smoothScrollTo(0, 0); // 点击后自动收起菜单
                    if (openedSwipeView == holder.hsvSwipe) {
                        openedSwipeView = null;
                    }
                }
            });
        } else {
            // 普通成员，或者户主看自己，直接隐藏后面的按钮，阻止其拉开
            holder.flDeleteContainer.setVisibility(View.GONE);
            holder.cardDelete.setOnClickListener(null);
        }
    }

    @Override
    public int getItemCount() {
        return memberList.size();
    }

    // 自定义接口，暴露移出按钮的点击事件
    public interface OnMemberDeleteListener {
        void onDeleteClick(int position, HomeMember member);
    }

    public static class HomeMember {
        public String userId;
        public String username;
        public boolean isOwner;
        public boolean isMe;
        private String avatarUrl;

        public HomeMember(String userId, String username, String avatarUrl, boolean isOwner, boolean isMe) {
            this.userId = userId;
            this.username = username;
            this.avatarUrl = avatarUrl;
            this.isOwner = isOwner;
            this.isMe = isMe;
        }
    }

    static class MemberViewHolder extends RecyclerView.ViewHolder {
        HorizontalScrollView hsvSwipe;
        FrameLayout flMainContent;
        FrameLayout flDeleteContainer;
        MaterialCardView cardDelete;
        ShapeableImageView avatarImageView;
        TextView tvUsername;
        TextView tvUserId;
        TextView tagOwner;
        TextView tagMe;

        public MemberViewHolder(@NonNull View itemView) {
            super(itemView);
            hsvSwipe = itemView.findViewById(R.id.hsv_swipe);
            flMainContent = itemView.findViewById(R.id.fl_main_content);
            flDeleteContainer = itemView.findViewById(R.id.fl_delete_container);
            cardDelete = itemView.findViewById(R.id.card_delete);
            avatarImageView = itemView.findViewById(R.id.iv_avatar);
            tvUsername = itemView.findViewById(R.id.tv_username);
            tvUserId = itemView.findViewById(R.id.tv_userid);
            tagOwner = itemView.findViewById(R.id.tag_owner);
            tagMe = itemView.findViewById(R.id.tag_me);
        }
    }

}